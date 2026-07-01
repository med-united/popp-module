package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError

/**
 * Verifies [jws] (JWS compact serialisation) against the keys in [jwks].
 *
 * Throws [PoppSdkError.Protocol] when the signature is invalid, the header
 * references a kid that does not appear in [jwks], or [jwks] contains no
 * usable EC P-256 key.
 *
 * Throws [PoppSdkError.PlatformUnsupported] on platforms where the
 * implementation has not been provided yet (currently iOS).
 */
internal expect fun verifyJwsSignature(
    jws: String,
    jwks: FederationJwks,
)
