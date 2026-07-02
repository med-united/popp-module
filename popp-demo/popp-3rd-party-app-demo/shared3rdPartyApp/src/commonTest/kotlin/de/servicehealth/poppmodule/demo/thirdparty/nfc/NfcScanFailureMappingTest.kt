package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlin.test.Test
import kotlin.test.assertEquals

class NfcScanFailureMappingTest {
    @Test
    fun maps_card_reasons() {
        assertEquals(
            NfcScanFailure.WRONG_CAN,
            PoppSdkError.Card(CardErrorReason.WRONG_CAN, "x").toNfcScanFailure(),
        )
        assertEquals(
            NfcScanFailure.CARD_LOST,
            PoppSdkError.Card(CardErrorReason.CARD_LOST, "x").toNfcScanFailure(),
        )
        assertEquals(
            NfcScanFailure.SECURE_CHANNEL,
            PoppSdkError.Card(CardErrorReason.SECURE_CHANNEL_FAILED, "x").toNfcScanFailure(),
        )
    }

    @Test
    fun maps_infrastructure_errors() {
        assertEquals(NfcScanFailure.NETWORK, PoppSdkError.Network("x").toNfcScanFailure())
        assertEquals(NfcScanFailure.PROTOCOL, PoppSdkError.Protocol("x").toNfcScanFailure())
        assertEquals(NfcScanFailure.UNKNOWN, PoppSdkError.PlatformUnsupported("x").toNfcScanFailure())
        assertEquals(NfcScanFailure.UNKNOWN, PoppSdkError.Unknown("x").toNfcScanFailure())
    }
}
