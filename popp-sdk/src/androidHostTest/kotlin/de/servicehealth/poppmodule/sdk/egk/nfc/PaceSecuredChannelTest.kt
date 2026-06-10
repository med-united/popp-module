package de.servicehealth.poppmodule.sdk.egk.nfc

import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaceSecuredChannelTest {

    @Test
    fun `establishes PACE lazily and round-trips logical APDUs over secure messaging`() = runTest {
        val card = FakePaceCard(can = "123456")
        val channel = PaceSecuredChannel(card, can = "123456")

        // case-1 APDU: echo responder returns the reconstructed plain command + 9000
        assertEquals("00A4040C9000", channel.transceive("00A4040C"))

        // second exchange proves the send-sequence counter stays in sync
        assertEquals("00D6000002CAFE9000", channel.transceive("00D6000002CAFE"))
    }

    @Test
    fun `round-trips multi-block payloads over secure messaging`() = runTest {
        val card = FakePaceCard(can = "123456")
        val channel = PaceSecuredChannel(card, can = "123456")

        // 32 bytes of command data: both directions span multiple AES-CBC blocks
        val data = "00112233445566778899AABBCCDDEEFF000102030405060708090A0B0C0D0E0F"
        assertEquals("00D6000020${data}9000", channel.transceive("00D6000020$data"))
    }

    @Test
    fun `wrong CAN surfaces as WRONG_CAN`() = runTest {
        val card = FakePaceCard(can = "123456")
        val channel = PaceSecuredChannel(card, can = "654321")

        val error = assertFailsWith<PoppSdkError.Card> { channel.transceive("00A4040C") }
        assertEquals(CardErrorReason.WRONG_CAN, error.reason)
    }

    @Test
    fun `tag loss surfaces as CARD_LOST`() = runTest {
        val card = FakePaceCard(can = "123456")
        val channel = PaceSecuredChannel(card, can = "123456")
        channel.transceive("00A4040C") // PACE up

        card.throwOnNextTransmit = IOException("tag moved away")
        val error = assertFailsWith<PoppSdkError.Card> { channel.transceive("00A4040C") }
        assertEquals(CardErrorReason.CARD_LOST, error.reason)
    }

    @Test
    fun `corrupted response MAC surfaces as SECURE_CHANNEL_FAILED`() = runTest {
        val card = FakePaceCard(can = "123456")
        val channel = PaceSecuredChannel(card, can = "123456")
        channel.transceive("00A4040C") // PACE up

        card.corruptNextResponseMac = true
        val error = assertFailsWith<PoppSdkError.Card> { channel.transceive("00A4040C") }
        assertEquals(CardErrorReason.SECURE_CHANNEL_FAILED, error.reason)
    }
}
