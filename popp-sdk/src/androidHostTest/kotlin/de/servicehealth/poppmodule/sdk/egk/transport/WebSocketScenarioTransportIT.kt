package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StartMessage
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import kotlin.test.assertTrue

/**
 * LOCAL / MANUAL integration test — not part of the CI gate.
 *
 * Prerequisites:
 *   1. Bring up the dockerized PoPP stack (`dev-local` profile: PoPP-Server + ZETA ingress +
 *      eGK-Hash-DB) from the gematik sample (`~/git/popp-sample-code`).
 *   2. Export the ingress URL, e.g.:
 *        export POPP_WS_URL="wss://localhost:443/ws"
 *   3. Run: ./gradlew :popp-sdk:testAndroidHostTest --tests "*WebSocketScenarioTransportIT"
 *
 * Without POPP_WS_URL the test is skipped (assumeTrue) so CI stays green.
 *
 * Scope of this assertion: connect → send Start → receive the first server StandardScenario,
 * proving the real transport's framing + (de)serialization against a live server. Completing the
 * loop to a Token additionally requires a virtual-card `EgkApduChannel` whose APDU responses come
 * from the sample's card fixtures (`popp-sample-code`); wiring that here is a follow-up (see the
 * full-loop note in §11 of the design doc).
 */
class WebSocketScenarioTransportIT {

    @Test
    fun connects_and_receives_first_scenario_from_docker_stack() {
        val url = System.getenv("POPP_WS_URL")
        assumeTrue("POPP_WS_URL not set — skipping local docker IT", url != null && url.isNotBlank())

        val transport = WebSocketScenarioTransport(
            client = createPoppWebSocketClient(disableTlsValidation = true),
            url = url!!,
        )
        runBlocking {
            transport.open()
            try {
                transport.send(StartMessage(clientSessionId = "it-" + System.currentTimeMillis()))
                val first: PoppMessage = transport.receive()
                assertTrue(
                    first is StandardScenarioMessage,
                    "expected a StandardScenario from the PoPP-Server, got ${first::class.simpleName}",
                )
            } finally {
                transport.close()
            }
        }
    }
}
