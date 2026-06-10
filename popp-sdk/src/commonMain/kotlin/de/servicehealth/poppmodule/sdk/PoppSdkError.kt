package de.servicehealth.poppmodule.sdk

/**
 * Errors raised by the PoPP SDK. Sealed so consumers can branch over the
 * meaningful failure modes that the ZETA layer produces (network, attestation,
 * configuration) without having to pattern-match on opaque exception types.
 */
sealed class PoppSdkError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** Network-layer failures: DNS, TLS, ZETA Guard unreachable, etc. */
    class Network(message: String, cause: Throwable? = null) : PoppSdkError(message, cause)

    /** Device attestation or registration at the ZETA Guard failed. */
    class Attestation(message: String, cause: Throwable? = null) : PoppSdkError(message, cause)

    /** The supplied [PoppSdkConfig] is rejected by the ZETA layer. */
    class Configuration(message: String, cause: Throwable? = null) : PoppSdkError(message, cause)

    /** Platform doesn't yet support PoPP — currently iOS, until zeta-sdk publishes a native variant. */
    class PlatformUnsupported(message: String) : PoppSdkError(message)

    /**
     * The PoPP-Service spoke something the loop can't honour: a malformed/unexpected message,
     * a class-discriminator we don't model, a sequence-counter replay/gap, or a card status word
     * outside the scenario's expected set. Distinct from [Network] (socket/TLS) — this is a
     * protocol-level violation, not a transport failure.
     */
    class Protocol(message: String, cause: Throwable? = null) : PoppSdkError(message, cause)

    /**
     * Card-level failure while talking to the eGK over NFC: PACE establishment, secure
     * messaging, or the tag connection itself. [reason] lets UIs (POPPM-135) distinguish
     * "CAN doesn't match this card" from "card moved away" without parsing messages.
     */
    class Card(
        val reason: CardErrorReason,
        message: String,
        cause: Throwable? = null,
    ) : PoppSdkError(message, cause)

    /** Catch-all for unexpected failures so they still come through the SDK's error type. */
    class Unknown(message: String, cause: Throwable? = null) : PoppSdkError(message, cause)
}

/** Why the eGK card channel failed. */
enum class CardErrorReason {
    /** PACE mutual authentication failed — the CAN does not belong to the presented card. */
    WRONG_CAN,

    /** The tag connection dropped mid-exchange (card moved away, NFC I/O error). */
    CARD_LOST,

    /** PACE/secure-messaging protocol failure: bad MAC, unexpected status word, malformed SM APDU. */
    SECURE_CHANNEL_FAILED,
}