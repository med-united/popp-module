package de.servicehealth.poppmodule.demo.thirdparty.can

import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeSecureStorage : SecureStorage {
    val backing = mutableMapOf<String, String>()

    override suspend fun put(
        key: String,
        value: String,
    ) {
        backing[key] = value
    }

    override suspend fun get(key: String): String? = backing[key]

    override suspend fun remove(key: String) {
        backing.remove(key)
    }

    override suspend fun clear() {
        backing.clear()
    }
}

class CanStoreTest {
    @Test fun savesAndLoadsCan() =
        runTest {
            val store = SecureStorageCanStore(FakeSecureStorage())
            store.save("123456")
            assertEquals("123456", store.load())
        }

    @Test fun clearRemovesCan() =
        runTest {
            val store = SecureStorageCanStore(FakeSecureStorage())
            store.save("123456")
            store.clear()
            assertNull(store.load())
        }

    @Test fun loadReturnsNullWhenEmpty() =
        runTest {
            assertNull(SecureStorageCanStore(FakeSecureStorage()).load())
        }

    @Test fun inMemoryStoreRoundTrips() =
        runTest {
            val store = InMemoryCanStore()
            store.save("654321")
            assertEquals("654321", store.load())
            store.clear()
            assertNull(store.load())
        }
}
