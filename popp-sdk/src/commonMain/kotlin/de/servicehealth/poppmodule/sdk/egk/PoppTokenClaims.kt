package de.servicehealth.poppmodule.sdk.egk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

// According to: de.gematik.refpopp.popp_server.scenario.common.token.EnumTokenClaimsKey
data class PoppTokenClaims(
    val patientProofTimeEpochSeconds: Long,
)

@Serializable
private class PoppTokenWire(
    val patientProofTime: Long? = null,
)

private val poppTokenJson = Json { ignoreUnknownKeys = true }

fun parsePoppTokenClaims(poppToken: String): PoppTokenClaims? {
    val payloadSegment = poppToken.split('.').getOrNull(1) ?: return null
    val payloadJson =
        try {
            val padded = payloadSegment.padEnd((payloadSegment.length + 3) / 4 * 4, '=')
            Base64.UrlSafe.decode(padded).decodeToString()
        } catch (_: Exception) {
            return null
        }
    val wire =
        try {
            poppTokenJson.decodeFromString<PoppTokenWire>(payloadJson)
        } catch (_: Exception) {
            return null
        }
    val patientProofTime = wire.patientProofTime ?: return null
    return PoppTokenClaims(patientProofTimeEpochSeconds = patientProofTime)
}
