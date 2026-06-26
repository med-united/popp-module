package de.servicehealth.poppmodule.sdk.federation

import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64URL
import de.servicehealth.poppmodule.sdk.PoppSdkError

internal actual fun verifyJwsSignature(
    jws: String,
    jwks: FederationJwks,
) {
    val jwsObject =
        try {
            JWSObject.parse(jws)
        } catch (e: Exception) {
            throw PoppSdkError.Protocol("Failed to parse JWS compact serialisation", e)
        }

    val kid = jwsObject.header.keyID
    val key = selectEc256Key(jwks, kid)

    val ecJwk =
        ECKey
            .Builder(Curve.P_256, Base64URL(key.x!!), Base64URL(key.y!!))
            .apply { if (key.kid != null) keyID(key.kid) }
            .build()

    // ECDSAVerifier handles R||S → DER conversion, algorithm–key-type mismatch
    // checks, and curve validation.
    val verifier = ECDSAVerifier(ecJwk)

    val valid =
        try {
            jwsObject.verify(verifier)
        } catch (e: Exception) {
            throw PoppSdkError.Protocol("JWS signature verification error", e)
        }

    if (!valid) {
        throw PoppSdkError.Protocol(
            "JWS signature does not match any key in the federation master JWKS",
        )
    }
}

private fun selectEc256Key(
    jwks: FederationJwks,
    kid: String?,
): FederationJwk {
    val candidates =
        jwks.keys.filter {
            it.kty == "EC" && (it.crv == null || it.crv == "P-256") && it.x != null && it.y != null
        }
    return when {
        kid != null ->
            candidates.find { it.kid == kid }
                ?: throw PoppSdkError.Protocol(
                    "No EC P-256 key with kid='$kid' in JWKS (available kids: ${jwks.keys.map { it.kid }})",
                )
        else ->
            candidates.firstOrNull()
                ?: throw PoppSdkError.Protocol("No EC P-256 key found in JWKS")
    }
}
