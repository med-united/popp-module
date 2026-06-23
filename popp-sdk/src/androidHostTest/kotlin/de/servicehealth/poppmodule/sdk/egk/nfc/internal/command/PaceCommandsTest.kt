package de.servicehealth.poppmodule.sdk.egk.nfc.internal.command

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalStdlibApi::class)
class PaceCommandsTest {
    private fun assertBytes(
        expectedHex: String,
        command: CommandApdu,
    ) =
        assertContentEquals(expectedHex.hexToByteArray(), command.bytes)

    @Test
    fun `select root requests FCP with wildcard le`() {
        assertBytes("00A4040400", PaceCommands.selectRoot(extendedLength = false))
        assertBytes("00A40404000000", PaceCommands.selectRoot(extendedLength = true))
    }

    @Test
    fun `select EF CardAccess by FID`() {
        assertBytes("00A4020C02011C", PaceCommands.selectEfCardAccess())
    }

    @Test
    fun `read binary from offset 0`() {
        assertBytes("00B0000000", PaceCommands.readBinary(extendedLength = false))
        assertBytes("00B00000000000", PaceCommands.readBinary(extendedLength = true))
    }

    @Test
    fun `mse set at selects PACE with CAN key reference 2`() {
        // OID = id-PACE-ECDH-GM-AES-CBC-CMAC-128; data = '80 0A OID || 83 01 02'
        assertBytes(
            "0022C1A40F800A04007F00070202040202830102",
            PaceCommands.mseSetAtPaceCan("04007F00070202040202".hexToByteArray()),
        )
    }

    @Test
    fun `general authenticate step 1 without payload`() {
        assertBytes("10860000027C0000", PaceCommands.generalAuthenticate(commandChaining = true))
    }

    @Test
    fun `general authenticate with tagged payload`() {
        assertBytes(
            "008600000C7C0A8508010203040506070800",
            PaceCommands.generalAuthenticate(false, "0102030405060708".hexToByteArray(), 5),
        )
    }

    @Test
    fun `transmitSuccessfully throws CardResponseException on non-9000`() {
        val channel =
            object : ICardChannel {
                override val maxTransceiveLength = 65535
                override val isExtendedLengthSupported = true

                override fun transmit(command: CommandApdu) = ResponseApdu(byteArrayOf(0x63, 0x00))
            }
        val e =
            assertFailsWith<CardResponseException> {
                channel.transmitSuccessfully("GENERAL AUTHENTICATE step 1", PaceCommands.generalAuthenticate(true))
            }
        assertEquals(0x6300, e.sw)
    }
}
