package de.servicehealth.poppmodule.sdk.qr

data class PoppCheckInPayload(
    val telematikId: String,
    val workplaceId: String? = null,
)
