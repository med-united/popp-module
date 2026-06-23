package de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CardResponseException
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CommandApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.ResponseApdu
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class TrustedChannelPaceKeyExchangeTest {
    /** Answers SELECT with 9000 but READ BINARY with an error SW and an empty data body. */
    private class ReadBinaryErrorCard : ICardChannel {
        override val maxTransceiveLength = 65535
        override val isExtendedLengthSupported = true

        override fun transmit(command: CommandApdu): ResponseApdu =
            when (command.bytes[1].toInt() and 0xFF) {
                0xA4 -> ResponseApdu(byteArrayOf(0x90.toByte(), 0x00)) // SELECT MF / EF.CardAccess
                0xB0 -> ResponseApdu(byteArrayOf(0x6A, 0x82.toByte())) // READ BINARY → File Not Found
                else -> error("unexpected INS 0x${(command.bytes[1].toInt() and 0xFF).toString(16)}")
            }
    }

    @Test
    fun `READ BINARY error SW surfaces the status word, not a downstream NPE`() =
        runTest {
            // An error SW returns no data; the handshake must report the real SW instead of
            // letting PaceInfo's ASN.1 parser fail with an opaque NullPointerException.
            val error =
                assertFailsWith<CardResponseException> {
                    ReadBinaryErrorCard().establishTrustedChannel("123456")
                }
            assertContains(error.message!!, "READ BINARY")
        }
}
