package de.servicehealth.poppmodule.sdk.egk.nfc

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CommandApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.EXPECTED_LENGTH_WILDCARD_EXTENDED
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.EXPECTED_LENGTH_WILDCARD_SHORT

/**
 * Parses a raw ISO/IEC 7816-4 command APDU — as delivered by the PoPP-Service inside a
 * scenario step — into a [CommandApdu] so it can be wrapped in secure messaging.
 * Supports cases 1, 2s, 2e, 3s, 3e, 4s, 4e.
 * Extended form with LC == 0x0000 is rejected.
 */
@Suppress("MagicNumber")
internal fun parseCommandApdu(raw: ByteArray): CommandApdu {
    require(raw.size >= 4) { "APDU must be at least 4 bytes, got ${raw.size}" }
    val cla = raw[0].toInt() and 0xFF
    val ins = raw[1].toInt() and 0xFF
    val p1 = raw[2].toInt() and 0xFF
    val p2 = raw[3].toInt() and 0xFF

    fun of(
        data: ByteArray?,
        ne: Int?,
    ) = CommandApdu.ofOptions(cla, ins, p1, p2, data, ne)

    if (raw.size == 4) return of(null, null) // case 1

    val b4 = raw[4].toInt() and 0xFF
    if (raw.size == 5) return of(null, if (b4 == 0) EXPECTED_LENGTH_WILDCARD_SHORT else b4) // case 2s

    if (b4 != 0) { // short form with data
        val nc = b4
        return when (raw.size) {
            5 + nc -> of(raw.copyOfRange(5, 5 + nc), null) // case 3s
            6 + nc -> { // case 4s
                val le = raw[5 + nc].toInt() and 0xFF
                of(raw.copyOfRange(5, 5 + nc), if (le == 0) EXPECTED_LENGTH_WILDCARD_SHORT else le)
            }
            else -> throw IllegalArgumentException("malformed short APDU: nc=$nc, size=${raw.size}")
        }
    }

    // extended form (b4 == 0x00)
    if (raw.size == 7) { // case 2e
        val le = ((raw[5].toInt() and 0xFF) shl 8) or (raw[6].toInt() and 0xFF)
        return of(null, if (le == 0) EXPECTED_LENGTH_WILDCARD_EXTENDED else le)
    }
    require(raw.size > 7) { "malformed extended APDU: size=${raw.size}" }
    val nc = ((raw[5].toInt() and 0xFF) shl 8) or (raw[6].toInt() and 0xFF)
    require(nc > 0) { "malformed extended APDU: LC must not be 0x0000" }
    return when (raw.size) {
        7 + nc -> of(raw.copyOfRange(7, 7 + nc), null) // case 3e
        9 + nc -> { // case 4e
            val le = ((raw[7 + nc].toInt() and 0xFF) shl 8) or (raw[8 + nc].toInt() and 0xFF)
            of(raw.copyOfRange(7, 7 + nc), if (le == 0) EXPECTED_LENGTH_WILDCARD_EXTENDED else le)
        }
        else -> throw IllegalArgumentException("malformed extended APDU: nc=$nc, size=${raw.size}")
    }
}
