package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.ErrorMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioResponseMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioStep
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StartMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.TokenMessage
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EgkReadDriverTest {
    private val sessionId = "fixed-session"

    private fun scenario(
        seq: Int,
        steps: List<ScenarioStep>,
        timeSpan: Int = 1000,
    ) =
        StandardScenarioMessage("1.0.0", sessionId, seq, timeSpan, steps)

    private fun driver(
        transport: FakeTransport,
        card: FakeCard,
    ) =
        EgkReadDriver(transport, card, newSessionId = { sessionId })

    @Test
    fun full_exchange_yields_success_and_sends_expected_messages() =
        runTest {
            val cmd = "00A4040C"
            val transport =
                FakeTransport(
                    listOf(
                        scenario(0, listOf(ScenarioStep(cmd, listOf("9000")))),
                        TokenMessage("jwt", "pn"),
                    ),
                )
            val card = FakeCard(responsesByCommand = mapOf(cmd to "9000"))

            val result = driver(transport, card).run(onProgress = {})

            assertEquals(EgkCheckInResult.Success("jwt", "pn"), result)
            assertEquals(
                listOf(
                    StartMessage(clientSessionId = sessionId),
                    ScenarioResponseMessage(listOf("9000")),
                ),
                transport.sent,
            )
            assertEquals(listOf(cmd), card.transceived)
            assertTrue(transport.opened)
            assertTrue(transport.closed)
        }

    @Test
    fun server_error_yields_failed() =
        runTest {
            val transport = FakeTransport(listOf(ErrorMessage("egk_check_failed", "bad")))
            val result = driver(transport, FakeCard()).run(onProgress = {})
            assertEquals(EgkCheckInResult.Failed("egk_check_failed", "bad"), result)
            assertTrue(transport.closed)
        }

    @Test
    fun status_word_mismatch_throws_protocol_and_closes() =
        runTest {
            val cmd = "00A4040C"
            val transport = FakeTransport(listOf(scenario(0, listOf(ScenarioStep(cmd, listOf("9000"))))))
            val card = FakeCard(responsesByCommand = mapOf(cmd to "6A82"))

            assertFailsWith<PoppSdkError.Protocol> { driver(transport, card).run(onProgress = {}) }
            assertTrue(transport.closed)
        }

    @Test
    fun throwing_transceive_surfaces_as_poppsdkerror_and_closes() =
        runTest {
            val cmd = "00A4040C"
            val transport = FakeTransport(listOf(scenario(0, listOf(ScenarioStep(cmd, listOf("9000"))))))
            val card = FakeCard(throwOnCommand = cmd)

            // A raw Throwable from the card must be wrapped as PoppSdkError.Unknown (not leaked, not Protocol).
            val error = assertFailsWith<PoppSdkError> { driver(transport, card).run(onProgress = {}) }
            assertIs<PoppSdkError.Unknown>(error)
            assertTrue(transport.closed)
        }

    @Test
    fun progress_is_emitted_per_step() =
        runTest {
            val transport =
                FakeTransport(
                    listOf(
                        scenario(0, listOf(ScenarioStep("AA", listOf("9000")), ScenarioStep("BB", listOf("9000")))),
                        TokenMessage("jwt", "pn"),
                    ),
                )
            val progress = mutableListOf<EgkProgress>()
            driver(transport, FakeCard()).run(onProgress = { progress += it })
            assertEquals(
                listOf(EgkProgress(0, 0, 2), EgkProgress(0, 1, 2)),
                progress,
            )
        }

    @Test
    fun silent_server_times_out_as_network_and_closes() =
        runTest {
            // A transport whose receive() never returns: the driver's receiveNext() timeout is the
            // only liveness guard. runTest's virtual clock fires withTimeout deterministically.
            var closed = false
            val transport =
                object : PoppServiceTransport {
                    override suspend fun open() = Unit

                    override suspend fun send(message: PoppMessage) = Unit

                    override suspend fun receive(): PoppMessage = awaitCancellation()

                    override suspend fun close() {
                        closed = true
                    }
                }

            val error =
                assertFailsWith<PoppSdkError.Network> {
                    EgkReadDriver(transport, FakeCard(), newSessionId = { sessionId }).run(onProgress = {})
                }
            assertTrue(error.message!!.contains("timed out"))
            assertTrue(closed)
        }
}
