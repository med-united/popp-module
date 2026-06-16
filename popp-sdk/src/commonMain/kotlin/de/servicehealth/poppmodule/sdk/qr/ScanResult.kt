package de.servicehealth.poppmodule.sdk.qr

sealed interface ScanResult {
    data class Valid(val payload: PoppCheckInPayload) : ScanResult

    data class Invalid(val reason: Reason) : ScanResult {
        enum class Reason {
            NOT_UTF8,
            NOT_JSON,
            WRONG_TYPE,
            MISSING_TID,
        }
    }
}
