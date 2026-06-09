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
 *   1. Bring up the dockerized PoPP stack from the gematik sample (`~/git/popp-sample-code`):
 *        cd ~/git/popp-sample-code
 *        # build the local images (DOCKER_HOST points the fabric8 plugin at the Docker Desktop socket)
 *        DOCKER_HOST=unix://$HOME/.docker/desktop/docker.sock \
 *          ./mvnw install -Dskip.dockerbuild=false -DskipTests=true
 *        docker compose -f docker/compose.yaml up -d   # default profile (no popp-client)
 *   2. Point at the PoPP-Server's WebSocket DIRECTLY (plain HTTP on 8443, bypasses ZETA):
 *        export POPP_WS_URL="ws://localhost:8443/ws"
 *   3. Run (use --no-daemon so POPP_WS_URL reaches the forked test JVM):
 *        ./gradlew --no-daemon :popp-sdk:testAndroidHostTest --tests "*WebSocketScenarioTransportIT"
 *
 * Why direct, not the ingress: the ZETA ingress (`wss://localhost:443/ws`) enforces auth on /ws
 * (`pep on`), so an unauthenticated upgrade returns HTTP 401 ("access token error: no token").
 * Reaching the loop through the ingress needs a ZETA-authenticated transport (DCR + SMC-B-signed
 * subject token + DPoP) — a documented follow-up. The PoPP-Server serves /ws on 8443 without auth,
 * which is enough for this test's connect/framing/(de)serialization scope.
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
