package de.servicehealth.poppmodule.sdk.egk.protocol

import kotlinx.serialization.json.Json

/**
 * The single [Json] configuration shared by the DTOs and the WebSocket transport.
 *
 * - `ignoreUnknownKeys` mirrors the sample's `@JsonIgnoreProperties(ignoreUnknown = true)` so the
 *   server adding fields never breaks the client.
 * - `explicitNulls = false` keeps optional nulls (e.g. `errorDetail`) off the wire.
 * - `encodeDefaults = true` so defaulted fields (`version`, `cardConnectionType`) are still sent.
 * - `classDiscriminator = "type"` matches the `@JsonClassDiscriminator("type")` on [PoppMessage].
 */
internal object PoppJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        classDiscriminator = "type"
    }
}
