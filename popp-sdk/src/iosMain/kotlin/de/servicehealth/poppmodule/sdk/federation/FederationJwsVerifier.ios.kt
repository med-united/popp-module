package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError

/**
 * iOS implementation pending.
 * Use SecKeyCreateWithData (uncompressed point 0x04||x||y, kSecAttrKeyTypeEC / kSecAttrKeyClassPublic)
 * then SecKeyVerifySignature(key, kSecKeyAlgorithmECDSASignatureMessageX962SHA256, input, sig, &error),
 * converting the raw R||S signature to X9.62 DER before the call.
 *
 * Until implemented, doFetch() catches PlatformUnsupported and falls back to HTTPS transport security.
 */
internal actual fun verifyJwsSignature(
    jws: String,
    jwks: FederationJwks,
): Unit =
    throw PoppSdkError.PlatformUnsupported(
        "JWS signature verification is not yet implemented for iOS",
    )
