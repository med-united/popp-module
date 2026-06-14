package de.servicehealth.poppmodule.sdk.internal

import de.gematik.zeta.sdk.storage.SdkStorage
import de.servicehealth.poppmodule.sdk.storage.SecureStorage

/**
 * Adapts our cross-platform [SecureStorage] to the zeta-sdk's [SdkStorage]
 * interface so the ZETA layer transparently persists into EncryptedSharedPreferences.
 */
internal class SdkStorageAdapter(private val delegate: SecureStorage) : SdkStorage {
    override suspend fun put(
        key: String,
        value: String,
    ) = delegate.put(key, value)

    override suspend fun get(key: String): String? = delegate.get(key)

    override suspend fun remove(key: String) = delegate.remove(key)

    override suspend fun clear() = delegate.clear()
}
