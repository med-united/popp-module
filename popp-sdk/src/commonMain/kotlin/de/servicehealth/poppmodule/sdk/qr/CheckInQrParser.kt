package de.servicehealth.poppmodule.sdk.qr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val POPP_CHECKIN_TYPE: String = "popp-checkin"

@Serializable
private class CheckInQrWire(
    val tid: String? = null,
    val typ: String? = null,
    val wpid: String? = null,
)

private val checkInJson = Json {
    ignoreUnknownKeys = false
    isLenient = false
}

fun parseCheckInPayload(raw: ByteArray): ScanResult {
    val text = try {
        raw.decodeToString(throwOnInvalidSequence = true)
    } catch (_: Exception) {
        return ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_UTF8)
    }
    return parseCheckInPayload(text)
}

fun parseCheckInPayload(text: String): ScanResult {
    val wire = try {
        checkInJson.decodeFromString<CheckInQrWire>(text)
    } catch (_: Exception) {
        return ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON)
    }

    if (wire.typ != POPP_CHECKIN_TYPE) {
        return ScanResult.Invalid(ScanResult.Invalid.Reason.WRONG_TYPE)
    }

    val telematikId = wire.tid
    if (telematikId.isNullOrBlank()) {
        return ScanResult.Invalid(ScanResult.Invalid.Reason.MISSING_TID)
    }

    val workplaceId = wire.wpid?.takeIf { it.isNotBlank() }
    return ScanResult.Valid(PoppCheckInPayload(telematikId = telematikId, workplaceId = workplaceId))
}
