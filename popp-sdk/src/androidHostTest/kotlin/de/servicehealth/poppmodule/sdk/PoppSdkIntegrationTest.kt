package de.servicehealth.poppmodule.sdk

import de.servicehealth.poppmodule.sdk.PoppSdkIntegrationTest.Companion.FQDN_PROPERTY
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Integration tests that exercise the full PoPP SDK stack against a real server.
 *
 * Skipped automatically when [FQDN_PROPERTY] is not set, so they never break CI.
 *
 * Run against a specific server:
 *
 *   # Local docker-compose stack (self-signed cert — pass the CA PEM file exported from the stack)
 *   ./gradlew :popp-sdk:testAndroidHostTest -Pintegration \
 *       -Dpopp.integration.fqdn="wss://popp-zeta-ingress:443/ws" \
 *       -Dpopp.integration.ca.pem.file="/path/to/ca.pem"
 *
 *   # RISE dev server
 *   ./gradlew :popp-sdk:testAndroidHostTest -Pintegration \
 *       -Dpopp.integration.fqdn="wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc"
 *
 * Note: hello() uses the device engine with DeviceOnlyTokenProvider (hardcoded JWT placeholder).
 * Whether the server accepts this token depends on its configuration. Once the PoPP spec
 * clarifies the device-level access token format, replace DeviceOnlyTokenProvider accordingly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PoppSdkIntegrationTest {

    companion object {
        const val FQDN_PROPERTY = "popp.integration.fqdn"
    }

    @Test
    fun hello_completes_against_real_server() {
        val fqdn = System.getProperty(FQDN_PROPERTY)
        assertNotNull(fqdn, "Must pass -D$FQDN_PROPERTY=<wss://...> to run the test.")

        val sdk = PoppSdk(
            context = PoppSdkContext(RuntimeEnvironment.getApplication()),
            storageOverride = InMemorySecureStorage(),
        )
        sdk.init(fqdn)

        runBlocking { sdk.hello() }
    }
}

private class InMemorySecureStorage : SecureStorage {
    private val map = ConcurrentHashMap<String, String>()
    override suspend fun put(key: String, value: String) { map[key] = value }
    override suspend fun get(key: String): String? = map[key]
    override suspend fun remove(key: String) { map.remove(key) }
    override suspend fun clear() { map.clear() }
}