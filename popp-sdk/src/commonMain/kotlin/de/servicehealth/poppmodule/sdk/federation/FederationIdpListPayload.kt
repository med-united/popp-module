package de.servicehealth.poppmodule.sdk.federation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FederationIdpListPayload(
    @SerialName("idp_entity")
    val idpEntities: List<FederationIdpEntry>,
)

@Serializable
internal data class FederationIdpEntry(
    val iss: String,
    @SerialName("organization_name")
    val organizationName: String,
    @SerialName("logo_uri")
    val logoUri: String? = null,
)
