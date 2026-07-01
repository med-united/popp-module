package de.servicehealth.poppmodule.demo.thirdparty.auth

import java.security.MessageDigest
import java.security.SecureRandom

actual object PkceGenerator {
    private val secureRandom = SecureRandom()

    actual fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return bytes.encodeBase64Url()
    }

    actual fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.encodeToByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes)
        val digest = md.digest()
        return digest.encodeBase64Url()
    }
}
