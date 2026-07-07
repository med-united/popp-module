package de.servicehealth.poppmodule.sdk.federation

import kotlinx.serialization.Serializable

@Serializable
internal data class FederationJwks(
    val keys: List<FederationJwk>,
)

@Serializable
internal data class FederationJwk(
    val kty: String,
    val crv: String? = null,
    val x: String? = null,
    val y: String? = null,
    val kid: String? = null,
    val use: String? = null,
    val alg: String? = null,
)
