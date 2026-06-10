package de.servicehealth.poppmodule.sdk.egk.nfc

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalStdlibApi::class)
class RawCommandApduTest {

    private fun roundTrip(hex: String): String =
        parseCommandApdu(hex.hexToByteArray()).bytes.toHexString(HexFormat.UpperCase)

    @Test
    fun `case 1 - header only`() {
        assertContentEquals("00A4040C".hexToByteArray(), roundTrip("00A4040C").hexToByteArray())
    }

    @Test
    fun `case 2s - le present`() {
        assertContentEquals("00B000001C".hexToByteArray(), roundTrip("00B000001C").hexToByteArray())
    }

    @Test
    fun `case 2s - le zero means 256`() {
        assertContentEquals("00B0000000".hexToByteArray(), roundTrip("00B0000000".uppercase()).hexToByteArray())
    }

    @Test
    fun `case 3s - data no le`() {
        assertContentEquals("00D6000002CAFE".hexToByteArray(), roundTrip("00D6000002CAFE").hexToByteArray())
    }

    @Test
    fun `case 4s - data and le`() {
        assertContentEquals("0086000002CAFE00".hexToByteArray(), roundTrip("0086000002CAFE00").hexToByteArray())
    }

    @Test
    fun `case 2e - extended le`() {
        assertContentEquals("00B0000000010A".hexToByteArray(), roundTrip("00B0000000010A").hexToByteArray())
    }

    @Test
    fun `malformed apdu is rejected`() {
        assertFailsWith<IllegalArgumentException> { parseCommandApdu("00A4".hexToByteArray()) }
        assertFailsWith<IllegalArgumentException> { parseCommandApdu("00D6000005CAFE".hexToByteArray()) }
    }

    @Test
    fun `case 2e - wildcard le`() {
        assertContentEquals("00B00000000000".hexToByteArray(), roundTrip("00B00000000000").hexToByteArray())
    }

    @Test
    fun `case 3e - extended data no le`() {
        val data = ByteArray(256) { it.toByte() }
        val raw = "00D60000000100".hexToByteArray() + data
        assertContentEquals(raw, parseCommandApdu(raw).bytes)
    }

    @Test
    fun `case 4e - extended data and le`() {
        val data = ByteArray(256) { it.toByte() }
        val raw = "00D60000000100".hexToByteArray() + data + "001A".hexToByteArray()
        assertContentEquals(raw, parseCommandApdu(raw).bytes)
    }

    @Test
    fun `case 3e with short nc is canonicalised to 3s`() {
        // 9-byte extended-form APDU with nc=2: parses fine, re-encodes in the equivalent short form
        assertContentEquals("00D6000002CAFE".hexToByteArray(), parseCommandApdu("00D60000000002CAFE".hexToByteArray()).bytes)
    }

    @Test
    fun `malformed extended apdu is rejected`() {
        // size matches neither 7+nc (3e) nor 9+nc (4e)
        assertFailsWith<IllegalArgumentException> { parseCommandApdu("00D600000002CAFEFF".hexToByteArray()) }
        // extended LC of 0x0000 is invalid per ISO 7816-4
        assertFailsWith<IllegalArgumentException> { parseCommandApdu("00860000000000001A".hexToByteArray()) }
    }
}
