package de.servicehealth.poppmodule.sdk.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Runs on a real device or emulator to verify KeystoreSecureStorage against
 * the actual Android Keystore — no fake provider, no Robolectric.
 *
 * Run with:
 *   ./gradlew :popp-sdk:androidConnectedCheck
 */
@RunWith(AndroidJUnit4::class)
class KeystoreSecureStorageDeviceTest {
    private companion object {
        const val TEST_NAMESPACE = "device-test-secure-storage"
    }

    private lateinit var context: PoppSdkContext
    private lateinit var storage: KeystoreSecureStorage

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        context = PoppSdkContext(appContext)
        storage = KeystoreSecureStorage(context, TEST_NAMESPACE)
        runBlocking { storage.clear() }
    }

    @Test
    fun get_returns_null_for_nonexistent_key() {
        runBlocking {
            assertNull(storage.get("missing"))
        }
    }

    @Test
    fun put_and_get_returns_decrypted_value() {
        runBlocking {
            storage.put("key1", "value1")
            assertEquals("value1", storage.get("key1"))
        }
    }

    @Test
    fun put_overwrites_existing_value() {
        runBlocking {
            storage.put("key1", "original")
            storage.put("key1", "updated")
            assertEquals("updated", storage.get("key1"))
        }
    }

    @Test
    fun put_and_get_empty_string_value() {
        runBlocking {
            storage.put("empty", "")
            assertEquals("", storage.get("empty"))
        }
    }

    @Test
    fun put_and_get_unicode_and_special_characters() {
        runBlocking {
            val value = "Hello, 世界! \n\t Special: !@#\$%^&*()"
            storage.put("unicode", value)
            assertEquals(value, storage.get("unicode"))
        }
    }

    @Test
    fun remove_clears_specific_key_and_leaves_others_intact() {
        runBlocking {
            storage.put("key1", "value1")
            storage.put("key2", "value2")
            storage.remove("key1")
            assertNull(storage.get("key1"))
            assertEquals("value2", storage.get("key2"))
        }
    }

    @Test
    fun remove_nonexistent_key_does_not_throw() {
        runBlocking {
            storage.remove("nonexistent")
        }
    }

    @Test
    fun clear_removes_all_stored_keys() {
        runBlocking {
            storage.put("key1", "value1")
            storage.put("key2", "value2")
            storage.clear()
            assertNull(storage.get("key1"))
            assertNull(storage.get("key2"))
        }
    }

    @Test
    fun multiple_keys_are_stored_independently() {
        runBlocking {
            storage.put("alpha", "1")
            storage.put("beta", "2")
            storage.put("gamma", "3")
            assertEquals("1", storage.get("alpha"))
            assertEquals("2", storage.get("beta"))
            assertEquals("3", storage.get("gamma"))
        }
    }

    @Test
    fun different_namespaces_do_not_share_data() {
        runBlocking {
            val other = KeystoreSecureStorage(context, "device-test-other-namespace")
            other.clear()
            storage.put("key", "from-first")
            other.put("key", "from-second")
            assertEquals("from-first", storage.get("key"))
            assertEquals("from-second", other.get("key"))
        }
    }

    @Test
    fun stored_value_in_shared_preferences_is_not_plaintext() {
        runBlocking {
            val plaintext = "sensitive-plaintext"
            storage.put("secret", plaintext)
            val prefs =
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getSharedPreferences(TEST_NAMESPACE, android.content.Context.MODE_PRIVATE)
            val raw = prefs.getString("secret", null)
            assertNotNull(raw)
            assertNotEquals(plaintext, raw)
        }
    }

    @Test
    fun keystore_key_persists_across_storage_instances() {
        runBlocking {
            storage.put("persistent", "hello")
            // Recreate the storage instance — same namespace reuses the existing keystore key
            val storage2 = KeystoreSecureStorage(context, TEST_NAMESPACE)
            assertEquals("hello", storage2.get("persistent"))
        }
    }

    @Test
    fun create_secure_storage_factory_returns_working_instance() {
        runBlocking {
            val created = createSecureStorage(context, "device-factory-test-namespace")
            created.put("hello", "world")
            assertEquals("world", created.get("hello"))
            created.clear()
        }
    }
}
