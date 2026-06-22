package de.servicehealth.poppmodule.sdk.federation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FederationEntityStatement(
    val metadata: FederationMetadata,
    val jwks: FederationJwks? = null,
)

@Serializable
internal data class FederationMetadata(
    @SerialName("federation_master")
    val federationMaster: FederationMasterMetadata? = null,
)

@Serializable
internal data class FederationMasterMetadata(
    @SerialName("idp_list_endpoint")
    val idpListEndpoint: String,
)
