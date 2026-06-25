package de.servicehealth.poppmodule.sdk.federation

import kotlinx.serialization.Serializable

@Serializable
data class FederationIdp(
    val entityId: String,
    val name: String,
    val logoUri: String? = null,
)
