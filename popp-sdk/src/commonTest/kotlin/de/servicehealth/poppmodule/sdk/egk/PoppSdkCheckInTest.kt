package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioStep
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.TokenMessage
import de.servicehealth.poppmodule.sdk.internal.ZetaEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PoppSdkCheckInTest {
    private val sessionId = "wired-session"

    private class FakeEngine : ZetaEngine {
        override suspend fun start() = Unit

        override suspend fun status(): String = "READY"
    }

    private fun startedSdk(transport: FakeTransport): PoppSdk =
        PoppSdk(
            engine = FakeEngine(),
            fqdn = "wss://test/ws",
            trustedCaPem = null,
            transportFactory = { _, _ -> transport },
            newSessionId = { sessionId },
        )

    @Test
    fun check_in_drives_loop_to_success() =
        runTest {
            val cmd = "00A4040C"
            val transport =
                FakeTransport(
                    listOf(
                        StandardScenarioMessage("1.0.0", sessionId, 0, 1000, listOf(ScenarioStep(cmd, listOf("9000")))),
                        TokenMessage("jwt", "pn"),
                    ),
                )
            val result =
                startedSdk(transport).checkInWithEgk(
                    channel = FakeCard(responsesByCommand = mapOf(cmd to "9000")),
                )
            assertEquals(EgkCheckInResult.Success("jwt", "pn"), result)
        }

    @Test
    fun check_in_on_unstarted_sdk_throws_configuration() =
        runTest {
            assertFailsWith<PoppSdkError.Configuration> {
                PoppSdk().checkInWithEgk(channel = FakeCard())
            }
        }

    @Test
    fun check_in_without_fqdn_throws_configuration() =
        runTest {
            // Engine present but no fqdn (only reachable through the internal constructor): the guard
            // must fire before any transport is built.
            val sdk =
                PoppSdk(
                    engine = FakeEngine(),
                    fqdn = null,
                    trustedCaPem = null,
                    transportFactory = { _, _ -> error("transport must not be built when fqdn is null") },
                    newSessionId = { sessionId },
                )
            assertFailsWith<PoppSdkError.Configuration> {
                sdk.checkInWithEgk(channel = FakeCard())
            }
        }
}
