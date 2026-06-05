package de.servicehealth.poppmodule.sdk.storage

import de.servicehealth.poppmodule.sdk.PoppSdkContext

/**
 * Minimal key/value store that the ZETA layer needs for registration data,
 * attestation keys, and refresh material. Backed by EncryptedSharedPreferences
 * on Android and the iOS Keychain.
 */
interface SecureStorage {
    suspend fun put(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
    suspend fun clear()
}

/**
 * Build a platform-backed [SecureStorage]. [namespace] is used to scope the
 * underlying storage — e.g. the preferences file name on Android or the
 * `kSecAttrService` value on iOS — so multiple SDK consumers don't collide.
 */
expect fun createSecureStorage(
    context: PoppSdkContext,
    namespace: String,
): SecureStorage