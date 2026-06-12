package de.servicehealth.poppmodule.sdk.egk.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * The on-the-wire message envelope of the PoPP "scenario" WebSocket protocol, modelled verbatim
 * from the gematik reference implementation (`popp-sample-code`, tag R2.5.0). Polymorphic JSON,
 * discriminated by a `type` property (Jackson `@JsonTypeInfo` on the server side). Only the
 * mobile-NFC subset is modelled; `ConnectorScenarioMessage` (stationary connector setups) is out
 * of scope (YAGNI).
 *
 * These types are internal: the public surface of the eGK loop is the `egk` package's
 * `EgkApduChannel` / `EgkCheckInResult` / `EgkProgress` and `PoppSdk.checkInWithEgk`.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
internal sealed interface PoppMessage

/** Client → server handshake opener; the client owns [clientSessionId]. */
@Serializable
@SerialName("Start")
internal data class StartMessage(
    val version: String = PROTOCOL_VERSION,
    val clientSessionId: String,
    val cardConnectionType: CardConnectionType = CardConnectionType.CONTACTLESS,
) : PoppMessage

/** Server → client batch of command APDUs to run on the card. */
@Serializable
@SerialName("StandardScenario")
internal data class StandardScenarioMessage(
    val version: String,
    val clientSessionId: String,
    /** 0..32767, +1 per server message; used as an ordering/replay guard. */
    val sequenceCounter: Int,
    /** Budget in ms for the client to answer; 0 marks the last scenario. */
    val timeSpan: Int,
    val steps: List<ScenarioStep>,
) : PoppMessage

/** One command APDU plus the status words the card is allowed to answer with. */
@Serializable
internal data class ScenarioStep(
    val commandApdu: String,
    val expectedStatusWords: List<String>,
)

/** Client → server response APDUs; the i-th entry answers the i-th command of the last scenario. */
@Serializable
@SerialName("ScenarioResponse")
internal data class ScenarioResponseMessage(
    val steps: List<String>,
) : PoppMessage

/** Server → client terminal success: the PoPP token (JWT compact) plus the Prüfnachweis. */
@Serializable
@SerialName("Token")
internal data class TokenMessage(
    val token: String,
    val pn: String,
) : PoppMessage

/** Server → client terminal failure. */
@Serializable
@SerialName("Error")
internal data class ErrorMessage(
    val errorCode: String,
    val errorDetail: String? = null,
) : PoppMessage

/**
 * How the card is connected. Mobile NFC only uses [CONTACTLESS].
 *
 * The wire values are the spec / `popp-commons` `CardConnectionType` strings (e.g.
 * `contactless-standard`), not the Kotlin names — the PoPP-Server rejects anything else with a
 * JSON-processing error. The `-standard` suffix is the standalone (no-connector) case.
 */
@Serializable
internal enum class CardConnectionType {
    @SerialName("contactless-standard") CONTACTLESS,
    @SerialName("contact-standard") CONTACT,
}

internal const val PROTOCOL_VERSION: String = "1.0.0"
