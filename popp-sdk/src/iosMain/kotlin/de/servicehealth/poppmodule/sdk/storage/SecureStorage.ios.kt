package de.servicehealth.poppmodule.sdk.storage

import de.servicehealth.poppmodule.sdk.PoppSdkContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * iOS placeholder implementation. Real Keychain (Security framework) support
 * is intentionally deferred until the gematik zeta-sdk publishes an iOS
 * variant — see `PoppSdkError.PlatformUnsupported`. Until then this in-memory
 * map keeps the [SecureStorage] surface symmetric across platforms so the
 * façade and tests compile and behave the same on both targets.
 *
 * Process-lifetime only: values are lost when the host process exits.
 */
internal class InMemorySecureStorage : SecureStorage {

    private val mutex = Mutex()
    private val backing = mutableMapOf<String, String>()

    override suspend fun put(key: String, value: String) = mutex.withLock {
        backing[key] = value
        Unit
    }

    override suspend fun get(key: String): String? = mutex.withLock { backing[key] }

    override suspend fun remove(key: String) = mutex.withLock {
        backing.remove(key)
        Unit
    }

    override suspend fun clear() = mutex.withLock { backing.clear() }
}

@Suppress("unused")
actual fun createSecureStorage(
    context: PoppSdkContext,
    namespace: String,
): SecureStorage = InMemorySecureStorage()