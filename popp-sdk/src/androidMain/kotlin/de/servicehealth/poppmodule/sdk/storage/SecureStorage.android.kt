package de.servicehealth.poppmodule.sdk.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LENGTH = 12
private const val GCM_TAG_LENGTH = 128

internal class KeystoreSecureStorage(
    context: PoppSdkContext,
    namespace: String,
) : SecureStorage {
    private val prefs =
        context.androidContext
            .getSharedPreferences(namespace, Context.MODE_PRIVATE)

    private val keyAlias = "popp-sdk-$namespace"

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(keyAlias)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
                generateKey()
            }
        }
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun encrypt(plaintext: String): String {
        val cipher =
            Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, secretKey())
            }
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = bytes.copyOfRange(GCM_IV_LENGTH, bytes.size)
        val cipher =
            Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            }
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    override suspend fun put(
        key: String,
        value: String,
    ) =
        withContext(Dispatchers.IO) {
            prefs.edit().putString(key, encrypt(value)).apply()
        }

    override suspend fun get(key: String): String? =
        withContext(Dispatchers.IO) {
            prefs.getString(key, null)?.let { decrypt(it) }
        }

    override suspend fun remove(key: String) =
        withContext(Dispatchers.IO) {
            prefs.edit().remove(key).apply()
        }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
}

actual fun createSecureStorage(
    context: PoppSdkContext,
    namespace: String,
): SecureStorage = KeystoreSecureStorage(context, namespace)
