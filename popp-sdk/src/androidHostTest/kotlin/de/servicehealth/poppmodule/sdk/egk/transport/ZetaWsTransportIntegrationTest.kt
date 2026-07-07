package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.DeviceOnly
import de.servicehealth.poppmodule.sdk.PlatformIdentity
import de.servicehealth.poppmodule.sdk.PoppSdkConfig
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StartMessage
import de.servicehealth.poppmodule.sdk.internal.AndroidZetaEngine
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertTrue

/**
 * LOCAL / MANUAL integration test — opt-in via `-Pintegration`, NOT part of the CI gate. Connects
 * through ZETA to the dockerized PoPP stack and proves [ZetaWsTransport] reaches the first scenario
 * message (the real-socket coverage that gates removing the direct WebSocket transport).
 *
 * Why the `IntegrationTest` suffix + `-Pintegration`: `build.gradle.kts` forwards
 * `popp.integration.fqdn` and `popp.integration.ca.pem.file` to the forked test JVM ONLY inside the
 * `-Pintegration` branch (plain `-D…` does not reach the test JVM), and that branch runs only the
 * integration-test classes (those whose name contains `IntegrationTest`).
 *
 * Prerequisites (see project memory `run-init-zeta-it-local-stack`):
 *   1. Bring up the dockerized ZETA + PoPP stack from `~/git/popp-sample-code`.
 *   2. Add the `/etc/hosts` entry: `127.0.0.1 popp-zeta-ingress`
 *   3. Run (the ingress cert is self-signed; pass it as the trusted CA):
 *        ./gradlew --no-daemon :popp-sdk:testAndroidHostTest -Pintegration \
 *          --tests "*ZetaWsTransportIntegrationTest" \
 *          -Dpopp.integration.fqdn="wss://popp-zeta-ingress:443/ws" \
 *          -Dpopp.integration.ca.pem.file="$HOME/git/popp-sample-code/docker/zeta/ingress/certs/nginx.crt"
 *
 * Skipped (assumeTrue) when `popp.integration.fqdn` is absent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@Ignore(
    "DORMANT: ZETA-routed eGK scenario is blocked on a real device / eGK (user) token. With the " +
        "correct CA, start() (discover→register→authenticate) passes, but ws() does a per-resource " +
        "token request the placeholder DeviceOnly token isn't authorized for → 400 'keine Berechtigung'. " +
        "Re-enable when TokenProviderConfig.Egk is implemented (POPPM-119 follow-up).",
)
class ZetaWsTransportIntegrationTest {
    @Test
    fun connects_through_zeta_and_receives_first_scenario() {
        val fqdn = System.getProperty("popp.integration.fqdn")
        assumeTrue("popp.integration.fqdn not set — skipping local ZETA IT", !fqdn.isNullOrBlank())

        val engine = AndroidZetaEngine(deviceConfig(fqdn!!), InMemorySecureStorage())
        runBlocking {
            engine.start() // discover → register → authenticate
            val transport = engine.scenarioTransport()
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

    private fun deviceConfig(fqdn: String) =
        PoppSdkConfig(
            fqdn = fqdn,
            productId = "de.servicehealth.popp",
            productVersion = "0.0.1",
            clientName = "service-health-popp-module",
            platformIdentity =
                PlatformIdentity.Android(
                    packageName = "de.servicehealth.poppmodule",
                    sha256CertFingerprints = listOf("AA:BB:CC"),
                ),
            scopes = listOf("openid"),
            requiredRoleOid = "1.2.276.0.76.4.156",
            tokenProvider = DeviceOnly,
        )

    private class InMemorySecureStorage : SecureStorage {
        private val map = ConcurrentHashMap<String, String>()

        override suspend fun put(
            key: String,
            value: String,
        ) {
            map[key] = value
        }

        override suspend fun get(key: String): String? = map[key]

        override suspend fun remove(key: String) {
            map.remove(key)
        }

        override suspend fun clear() {
            map.clear()
        }
    }
}
