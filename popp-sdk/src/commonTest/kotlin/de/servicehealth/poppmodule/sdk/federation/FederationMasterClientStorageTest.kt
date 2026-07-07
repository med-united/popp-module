package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FederationMasterClientStorageTest {
    private val idp =
        FederationIdp(
            entityId = "https://idp.example",
            name = "Test Kasse",
            logoUri = "https://idp.example/logo.svg",
        )

    @Test
    fun saveSelectedIdp_and_loadSelectedIdp_roundtrip() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    storage = FakeSecureStorage(),
                    httpClient = noOpClient(),
                )
            client.saveSelectedIdp(idp)
            assertEquals(idp, client.loadSelectedIdp())
            client.close()
        }

    @Test
    fun loadSelectedIdp_returns_null_when_nothing_saved() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    storage = FakeSecureStorage(),
                    httpClient = noOpClient(),
                )
            assertNull(client.loadSelectedIdp())
            client.close()
        }

    @Test
    fun saveSelectedIdp_is_noop_when_storage_is_null() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    storage = null,
                    httpClient = noOpClient(),
                )
            client.saveSelectedIdp(idp) // must not throw
            assertNull(client.loadSelectedIdp())
            client.close()
        }

    @Test
    fun loadSelectedIdp_returns_null_when_storage_is_null() =
        runTest {
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    storage = null,
                    httpClient = noOpClient(),
                )
            assertNull(client.loadSelectedIdp())
            client.close()
        }

    @Test
    fun loadSelectedIdp_clears_and_returns_null_when_stored_json_is_corrupt() =
        runTest {
            val storage = FakeSecureStorage()
            // Plant a value that cannot be deserialized as FederationIdp.
            storage.put("federation-selected-idp", """{"unexpected":"schema"}""")
            val client =
                FederationMasterClient(
                    baseUrl = "https://fed.example",
                    storage = storage,
                    httpClient = noOpClient(),
                )
            // First load: parse fails → null returned and corrupt entry removed.
            assertNull(client.loadSelectedIdp())
            // Corrupt entry was cleared; the storage key is gone.
            assertNull(storage.get("federation-selected-idp"))
            client.close()
        }
}

private class FakeSecureStorage : SecureStorage {
    private val mutex = Mutex()
    private val map = mutableMapOf<String, String>()

    override suspend fun put(
        key: String,
        value: String,
    ) = mutex.withLock {
        map[key] = value
        Unit
    }

    override suspend fun get(key: String): String? = mutex.withLock { map[key] }

    override suspend fun remove(key: String) =
        mutex.withLock {
            map.remove(key)
            Unit
        }

    override suspend fun clear() = mutex.withLock { map.clear() }
}

private fun noOpClient(): HttpClient =
    HttpClient(MockEngine) {
        engine { addHandler { respond("", HttpStatusCode.OK) } }
    }
