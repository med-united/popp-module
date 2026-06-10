package de.servicehealth.poppmodule.sdk.egk.nfc

import android.nfc.tech.IsoDep
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CommandApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.ResponseApdu

/**
 * [ICardChannel] over Android's [IsoDep]. Connects lazily on the first transmit so
 * channel construction in onTagDiscovered stays free of I/O. Not unit-tested on the host
 * JVM (thin wrapper over framework I/O); verified on hardware (see docs/manual-egk-nfc-verification.md).
 */
internal class IsoDepTransport(private val isoDep: IsoDep) : ICardChannel {

    override val maxTransceiveLength: Int get() = isoDep.maxTransceiveLength
    override val isExtendedLengthSupported: Boolean get() = isoDep.isExtendedLengthApduSupported

    override fun transmit(command: CommandApdu): ResponseApdu {
        if (!isoDep.isConnected) {
            isoDep.connect()
            isoDep.timeout = ISO_DEP_TIMEOUT_MS
        }
        return ResponseApdu(isoDep.transceive(command.bytes))
    }

    private companion object {
        // Generous timeout: PACE's GENERAL AUTHENTICATE steps can take >1s on older cards.
        const val ISO_DEP_TIMEOUT_MS = 2500
    }
}
