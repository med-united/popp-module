package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.ErrorMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioResponseMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioStep
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StartMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.TokenMessage

/** Inputs to the pure protocol machine. */
internal sealed interface EgkEvent {
    /** Kick-off: produce the [StartMessage]. */
    data object Begin : EgkEvent

    /** A message arrived from the PoPP-Service. */
    data class ServerMessage(val message: PoppMessage) : EgkEvent

    /** The card answered the command APDUs of the current scenario (i-th ↔ i-th). */
    data class ApdusTransceived(val responsesHex: List<String>) : EgkEvent
}

/** Effects the driver must carry out; the machine itself performs no I/O. */
internal sealed interface EgkAction {
    /** Send [message] to the PoPP-Service. */
    data class Send(val message: PoppMessage) : EgkAction

    /** Run [steps] on the card; [scenarioIndex] is the 0-based scenario number (for progress). */
    data class Transceive(val steps: List<ScenarioStep>, val scenarioIndex: Int) : EgkAction

    /** Terminal success/failure business outcome. */
    data class Complete(val result: EgkCheckInResult) : EgkAction

    /** Terminal infrastructure/protocol failure. */
    data class Fail(val error: PoppSdkError) : EgkAction
}

/** Immutable protocol state. */
internal data class EgkScenarioState(
    val clientSessionId: String,
    val phase: Phase,
    val lastSequenceCounter: Int?,
    val scenarioIndex: Int,
) {
    enum class Phase { AwaitingStart, AwaitingScenario, AwaitingApdus, Terminal }

    companion object {
        fun initial(clientSessionId: String): EgkScenarioState =
            EgkScenarioState(clientSessionId, Phase.AwaitingStart, lastSequenceCounter = null, scenarioIndex = 0)
    }
}

/**
 * The pure eGK scenario protocol: `(state, event) -> (state, [action])`. No suspension, no I/O,
 * no clock — every branch is a fast, mock-free table test. The driver ([EgkReadDriver]) is the
 * imperative shell that runs the actions.
 */
internal object EgkScenarioStateMachine {
    fun next(
        state: EgkScenarioState,
        event: EgkEvent,
    ): Pair<EgkScenarioState, List<EgkAction>> =
        when (event) {
            EgkEvent.Begin -> onBegin(state)
            is EgkEvent.ServerMessage -> onServerMessage(state, event.message)
            is EgkEvent.ApdusTransceived -> onApdusTransceived(state, event.responsesHex)
        }

    private fun onBegin(state: EgkScenarioState): Pair<EgkScenarioState, List<EgkAction>> =
        if (state.phase != EgkScenarioState.Phase.AwaitingStart) {
            fail(state, "Begin received in phase ${state.phase}")
        } else {
            state.copy(phase = EgkScenarioState.Phase.AwaitingScenario) to
                listOf(EgkAction.Send(StartMessage(clientSessionId = state.clientSessionId)))
        }

    private fun onServerMessage(
        state: EgkScenarioState,
        message: PoppMessage,
    ): Pair<EgkScenarioState, List<EgkAction>> =
        when (message) {
            is TokenMessage ->
                state.copy(phase = EgkScenarioState.Phase.Terminal) to
                    listOf(EgkAction.Complete(EgkCheckInResult.Success(message.token, message.pn)))

            is ErrorMessage ->
                state.copy(phase = EgkScenarioState.Phase.Terminal) to
                    listOf(EgkAction.Complete(EgkCheckInResult.Failed(message.errorCode, message.errorDetail)))

            is StandardScenarioMessage -> onScenario(state, message)

            // StartMessage / ScenarioResponseMessage are client→server only — never inbound. Listed
            // explicitly (rather than `else`) so the compiler flags any future PoppMessage subtype.
            is StartMessage, is ScenarioResponseMessage ->
                fail(state, "unexpected server message ${message::class.simpleName} in phase ${state.phase}")
        }

    private fun onScenario(
        state: EgkScenarioState,
        message: StandardScenarioMessage,
    ): Pair<EgkScenarioState, List<EgkAction>> {
        if (state.phase != EgkScenarioState.Phase.AwaitingScenario) {
            return fail(state, "StandardScenario received in phase ${state.phase}")
        }
        if (message.clientSessionId != state.clientSessionId) {
            return fail(state, "clientSessionId mismatch: got ${message.clientSessionId}, expected ${state.clientSessionId}")
        }
        val expected = state.lastSequenceCounter?.plus(1)
        if (expected != null && message.sequenceCounter != expected) {
            return fail(state, "sequenceCounter out of order: got ${message.sequenceCounter}, expected $expected")
        }
        val index = state.scenarioIndex
        return if (message.steps.isEmpty()) {
            state.copy(
                phase = EgkScenarioState.Phase.AwaitingScenario,
                lastSequenceCounter = message.sequenceCounter,
                scenarioIndex = index + 1,
            ) to listOf(EgkAction.Send(ScenarioResponseMessage(emptyList())))
        } else {
            state.copy(
                phase = EgkScenarioState.Phase.AwaitingApdus,
                lastSequenceCounter = message.sequenceCounter,
                scenarioIndex = index + 1,
            ) to listOf(EgkAction.Transceive(message.steps, scenarioIndex = index))
        }
    }

    private fun onApdusTransceived(
        state: EgkScenarioState,
        responsesHex: List<String>,
    ): Pair<EgkScenarioState, List<EgkAction>> =
        if (state.phase != EgkScenarioState.Phase.AwaitingApdus) {
            fail(state, "ApdusTransceived received in phase ${state.phase}")
        } else {
            state.copy(phase = EgkScenarioState.Phase.AwaitingScenario) to
                listOf(EgkAction.Send(ScenarioResponseMessage(responsesHex)))
        }

    private fun fail(
        state: EgkScenarioState,
        reason: String,
    ): Pair<EgkScenarioState, List<EgkAction>> =
        state.copy(phase = EgkScenarioState.Phase.Terminal) to
            listOf(EgkAction.Fail(PoppSdkError.Protocol(reason)))
}
