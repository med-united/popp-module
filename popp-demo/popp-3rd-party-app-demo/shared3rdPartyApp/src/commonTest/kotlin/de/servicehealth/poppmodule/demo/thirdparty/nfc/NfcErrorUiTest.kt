package de.servicehealth.poppmodule.demo.thirdparty.nfc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NfcErrorUiTest {
    @Test
    fun every_failure_maps_to_distinct_non_trivial_ui() {
        val uis = NfcScanFailure.entries.map { it.toErrorUi() }

        assertEquals(
            NfcScanFailure.entries.size,
            uis.map { it.titleRes }.toSet().size,
            "every failure category should have a distinct title",
        )
        uis.forEach { assertTrue(it.titleRes != it.messageRes) }
    }

    @Test
    fun primary_recovery_matches_intent() {
        assertEquals(NfcErrorRecovery.REENTER_CAN, NfcScanFailure.WRONG_CAN.toErrorUi().primary)
        assertEquals(NfcErrorRecovery.RETRY, NfcScanFailure.CARD_LOST.toErrorUi().primary)
        assertEquals(NfcErrorRecovery.RETRY, NfcScanFailure.NETWORK.toErrorUi().primary)
        assertEquals(NfcErrorRecovery.CLOSE, NfcScanFailure.SERVER_REJECTED.toErrorUi().primary)
    }
}
