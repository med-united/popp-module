package de.servicehealth.poppmodule.sdk.egk

/**
 * An already-PACE-secured channel to a contactless eGK. The NFC layer (POPPM-119/135) implements
 * this: it establishes PACE with the CAN, then transceives logical command APDUs over secure
 * messaging. POPPM-118 only consumes it — it never sees the CAN and never runs PACE.
 *
 * @param commandApduHex ISO/IEC 7816-4 command APDU as a hex string.
 * @return the card's response APDU as a hex string (>= 4 hex chars; trailing SW1 SW2).
 * @throws Throwable on card removal / secure-messaging / PACE failure; aborts the loop.
 */
interface EgkApduChannel {
    suspend fun transceive(commandApduHex: String): String
}
