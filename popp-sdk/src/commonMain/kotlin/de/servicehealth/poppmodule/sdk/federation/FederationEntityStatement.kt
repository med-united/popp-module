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
    @SerialName("federation_entity")
    val federationEntity: FederationEntityMetadata? = null,
)

@Serializable
internal data class FederationEntityMetadata(
    @SerialName("idp_list_endpoint")
    val idpListEndpoint: String,
)
