package de.servicehealth.poppmodule.sdk.egk.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel

/**
 * Factory for the NFC eGK channel (POPPM-119, gemSpec_PoPP_Modul §3.3.9 sendAPDUCommands).
 *
 * The host app owns the NFC session (reader mode / foreground dispatch) and hands the
 * discovered [Tag] plus the card's CAN to [fromTag]; the returned channel is passed to
 * `PoppSdk.checkInWithEgk`. Construction is cheap — the ISO-DEP connection and the PACE
 * handshake happen lazily on the first transceive, on Dispatchers.IO.
 */
object EgkNfcChannel {
    /**
     * @param tag the NFC tag from e.g. NfcAdapter.ReaderCallback.onTagDiscovered
     * @param can the 6-digit Card Access Number printed on the eGK
     * @throws PoppSdkError.Card if the tag does not support ISO-DEP (ISO 14443-4)
     */
    fun fromTag(
        tag: Tag,
        can: String,
    ): EgkApduChannel {
        val isoDep =
            IsoDep.get(tag)
                ?: throw PoppSdkError.Card(
                    CardErrorReason.SECURE_CHANNEL_FAILED,
                    "NFC tag does not support ISO-DEP (ISO 14443-4) — not an eGK?",
                )
        return PaceSecuredChannel(IsoDepTransport(isoDep), can)
    }
}
