@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package de.servicehealth.poppmodule.demo.thirdparty.auth

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

actual object PkceGenerator {
    actual fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        bytes.usePinned { pinned ->
            val status = SecRandomCopyBytes(kSecRandomDefault, 32u, pinned.addressOf(0))
            if (status != 0) {
                // Fallback or throw if SecRandomCopyBytes fails. This is highly unlikely on iOS.
                throw RuntimeException("Failed to generate secure random bytes: status $status")
            }
        }
        return bytes.encodeBase64Url()
    }

    actual fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.encodeToByteArray()
        val digestBytes = ByteArray(CC_SHA256_DIGEST_LENGTH)

        bytes.usePinned { pinnedBytes ->
            digestBytes.usePinned { pinnedDigest ->
                CC_SHA256(
                    pinnedBytes.addressOf(0),
                    bytes.size.toUInt(),
                    pinnedDigest.addressOf(0).reinterpret(),
                )
            }
        }
        return digestBytes.encodeBase64Url()
    }
}
