package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioStep
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.TokenMessage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// PoppServiceTransport, FakeTransport and FakeCard are in this same package — no imports needed.
// checkInWithEgk uses the direct WebSocket transport (transportFactory); ZETA routing is dormant
// (see PoppSdk.checkInWithEgk TODO + ZetaWsTransportIntegrationTest), so these tests inject a
// FakeTransport rather than a fake ZetaEngine.
class PoppSdkCheckInTest {
    private val sessionId = "wired-session"

    private fun sdkWith(transport: PoppServiceTransport): PoppSdk =
        PoppSdk(
            transportFactory = { _, _ -> transport },
            newSessionId = { sessionId },
        )

    @Test
    fun check_in_via_public_path_drives_loop_to_success() =
        runTest {
            val cmd = "00A4040C"
            val transport =
                FakeTransport(
                    listOf(
                        StandardScenarioMessage(
                            "1.0.0",
                            sessionId,
                            0,
                            1000,
                            listOf(ScenarioStep(cmd, listOf("9000"))),
                        ),
                        TokenMessage("jwt", "pn"),
                    ),
                )
            val sdk = sdkWith(transport)
            sdk.init("wss://test/ws")

            val result = sdk.checkInWithEgk(channel = FakeCard(responsesByCommand = mapOf(cmd to "9000")))

            assertEquals(EgkCheckInResult.Success("jwt", "pn"), result)
        }

    @Test
    fun check_in_without_init_throws_configuration() =
        runTest {
            val sdk =
                PoppSdk(
                    transportFactory = { _, _ -> error("transport must not be built before init") },
                    newSessionId = { sessionId },
                )
            assertFailsWith<PoppSdkError.Configuration> {
                sdk.checkInWithEgk(channel = FakeCard())
            }
        }

    @Test
    fun init_with_different_fqdn_throws_configuration() =
        runTest {
            val sdk = sdkWith(FakeTransport(emptyList()))
            sdk.init("wss://a/ws")
            sdk.init("wss://a/ws") // same fqdn → no-op
            assertFailsWith<PoppSdkError.Configuration> {
                sdk.init("wss://b/ws")
            }
        }
}
