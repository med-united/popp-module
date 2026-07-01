package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val ALLOWED_ALGORITHMS =
    setOf(
        "ES256", "ES384", "ES512",
        "RS256", "RS384", "RS512",
        "PS256", "PS384", "PS512",
    )

@Serializable
internal data class JwtHeader(
    val alg: String,
    val kid: String? = null,
)

@OptIn(ExperimentalEncodingApi::class)
internal fun decodeJwtPayload(jwt: String): String {
    val segments = jwt.trim().split(".")
    if (segments.size < 3) {
        throw PoppSdkError.Protocol(
            "JWT must have 3 dot-separated segments, got ${segments.size}",
        )
    }
    val headerJson =
        try {
            Base64.UrlSafe.decode(addPadding(segments[0])).decodeToString()
        } catch (e: Exception) {
            throw PoppSdkError.Protocol("Failed to base64url-decode JWT header", e)
        }
    val header =
        try {
            FederationJson.instance.decodeFromString<JwtHeader>(headerJson)
        } catch (e: Exception) {
            throw PoppSdkError.Protocol("Failed to parse JWT header", e)
        }
    if (header.alg.equals("none", ignoreCase = true) || header.alg !in ALLOWED_ALGORITHMS) {
        throw PoppSdkError.Protocol(
            "JWT algorithm '${header.alg}' is not permitted; expected one of $ALLOWED_ALGORITHMS",
        )
    }
    return try {
        Base64.UrlSafe.decode(addPadding(segments[1])).decodeToString()
    } catch (e: Exception) {
        throw PoppSdkError.Protocol("Failed to base64url-decode JWT payload", e)
    }
}

internal fun addPadding(input: String): String =
    input + "=".repeat((4 - input.length % 4) % 4)
