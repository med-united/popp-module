package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * The imperative shell. Pumps [EgkScenarioStateMachine], runs its actions against the
 * [transport] and the [channel], and emits progress. The protocol logic lives entirely in the
 * machine; this class only does I/O, status-word checking, and timeout bounding.
 */
internal class EgkReadDriver(
    private val transport: PoppServiceTransport,
    private val channel: EgkApduChannel,
    private val newSessionId: () -> String,
) {
    suspend fun run(onProgress: (EgkProgress) -> Unit): EgkCheckInResult {
        var state = EgkScenarioState.initial(newSessionId())
        var receiveBudgetMs = DEFAULT_RECEIVE_MS
        val pending = ArrayDeque<EgkAction>()

        fun advance(event: EgkEvent) {
            val (s, actions) = EgkScenarioStateMachine.next(state, event)
            state = s
            pending.addAll(actions)
        }

        try {
            // open() is inside the try so the finally still runs (and closes) if a future
            // transport implementation throws after acquiring resources mid-open.
            transport.open()
            advance(EgkEvent.Begin)

            while (true) {
                while (pending.isNotEmpty()) {
                    when (val action = pending.removeFirst()) {
                        is EgkAction.Send -> transport.send(action.message)

                        is EgkAction.Complete -> return action.result

                        is EgkAction.Fail -> throw action.error

                        is EgkAction.Transceive ->
                            advance(EgkEvent.ApdusTransceived(runScenario(action, onProgress)))
                    }
                }

                // No terminal reached and nothing left to do locally: pull the next server message.
                val message = receiveNext(receiveBudgetMs)
                if (message is StandardScenarioMessage) {
                    receiveBudgetMs =
                        if (message.timeSpan > 0) {
                            message.timeSpan.toLong().coerceIn(MIN_RECEIVE_MS, MAX_RECEIVE_MS)
                        } else {
                            DEFAULT_RECEIVE_MS
                        }
                }
                advance(EgkEvent.ServerMessage(message))
            }
        } finally {
            transport.close()
        }
    }

    private suspend fun runScenario(
        action: EgkAction.Transceive,
        onProgress: (EgkProgress) -> Unit,
    ): List<String> {
        val responses = ArrayList<String>(action.steps.size)
        action.steps.forEachIndexed { i, step ->
            onProgress(EgkProgress(action.scenarioIndex, i, action.steps.size))
            val response =
                try {
                    channel.transceive(step.commandApdu)
                } catch (e: PoppSdkError) {
                    throw e
                } catch (e: CancellationException) {
                    throw e // never swallow structured-concurrency cancellation as an SDK error
                } catch (e: Throwable) {
                    throw PoppSdkError.Unknown("eGK transceive failed for command ${step.commandApdu}", e)
                }
            if (step.expectedStatusWords.isNotEmpty()) {
                val statusWord = response.takeLast(STATUS_WORD_HEX_LEN).uppercase()
                val allowed = step.expectedStatusWords.map { it.uppercase() }
                if (statusWord !in allowed) {
                    throw PoppSdkError.Protocol(
                        "unexpected status word $statusWord (expected one of $allowed)",
                    )
                }
            }
            responses += response
        }
        return responses
    }

    private suspend fun receiveNext(budgetMs: Long) =
        try {
            withTimeout(budgetMs) { transport.receive() }
        } catch (e: TimeoutCancellationException) {
            throw PoppSdkError.Network("timed out waiting for PoPP-Service message after ${budgetMs}ms", e)
        }

    private companion object {
        const val STATUS_WORD_HEX_LEN = 4
        const val DEFAULT_RECEIVE_MS = 30_000L
        const val MIN_RECEIVE_MS = 5_000L
        const val MAX_RECEIVE_MS = 120_000L
    }
}
