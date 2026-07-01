package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError

/** UI state of the eGK NFC scan (POPPM-161). Pure — no platform/SDK lifecycle. */
sealed interface NfcScanUiState {
    /** Reader enabled, waiting for the card (positioning guide shown). */
    data object WaitingForCard : NfcScanUiState

    /** Card detected; the check-in read loop is running. [percent] is 0..100. */
    data class Reading(val percent: Int) : NfcScanUiState

    /** PoPP-Service issued a token. */
    data class Succeeded(val poppToken: String, val pruefnachweis: String) : NfcScanUiState

    /** The scan ended in a (business or infrastructure) failure. */
    data class Failed(val reason: NfcScanFailure, val detail: String?) : NfcScanUiState
}

/** Failure category surfaced to the Error screen (POPPM-160). */
enum class NfcScanFailure {
    /** PoPP-Service rejected the card business-wise (e.g. UnknownCertificates). */
    SERVER_REJECTED,
    WRONG_CAN,
    CARD_LOST,
    SECURE_CHANNEL,
    NETWORK,
    PROTOCOL,
    UNKNOWN,
}

/** Maps an SDK error to a UI failure category. */
fun PoppSdkError.toNfcScanFailure(): NfcScanFailure =
    when (this) {
        is PoppSdkError.Card ->
            when (reason) {
                CardErrorReason.WRONG_CAN -> NfcScanFailure.WRONG_CAN
                CardErrorReason.CARD_LOST -> NfcScanFailure.CARD_LOST
                CardErrorReason.SECURE_CHANNEL_FAILED -> NfcScanFailure.SECURE_CHANNEL
            }
        is PoppSdkError.Network -> NfcScanFailure.NETWORK
        is PoppSdkError.Protocol -> NfcScanFailure.PROTOCOL
        else -> NfcScanFailure.UNKNOWN
    }
