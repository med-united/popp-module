package de.servicehealth.poppmodule.sdk.federation

import kotlinx.serialization.json.Json

internal object FederationJson {
    val instance: Json = Json { ignoreUnknownKeys = true }
}
