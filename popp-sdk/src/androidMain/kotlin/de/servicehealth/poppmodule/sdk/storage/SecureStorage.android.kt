package de.servicehealth.poppmodule.sdk.storage

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class EncryptedSharedPrefsSecureStorage(
    context: PoppSdkContext,
    namespace: String,
) : SecureStorage {

    private val prefs by lazy {
        val androidContext = context.androidContext
        val masterKey = MasterKey.Builder(androidContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            androidContext,
            namespace,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun put(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}

actual fun createSecureStorage(
    context: PoppSdkContext,
    namespace: String,
): SecureStorage = EncryptedSharedPrefsSecureStorage(context, namespace)