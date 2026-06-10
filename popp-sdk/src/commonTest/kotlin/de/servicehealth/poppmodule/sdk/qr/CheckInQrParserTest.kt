package de.servicehealth.poppmodule.sdk.qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CheckInQrParserTest {

    private val gematikReferenceJson = """{"tid":"1-234567890","typ":"popp-checkin","wpid":"REZEPTION-1"}"""

    @Test
    fun validPayloadWithWorkplaceIsParsed() {
        val result = parseCheckInPayload(gematikReferenceJson)
        val valid = assertIs<ScanResult.Valid>(result)
        assertEquals("1-234567890", valid.payload.telematikId)
        assertEquals("REZEPTION-1", valid.payload.workplaceId)
    }

    @Test
    fun validPayloadFromUtf8BytesIsParsed() {
        val result = parseCheckInPayload(gematikReferenceJson.encodeToByteArray())
        val valid = assertIs<ScanResult.Valid>(result)
        assertEquals("1-234567890", valid.payload.telematikId)
        assertEquals("REZEPTION-1", valid.payload.workplaceId)
    }

    @Test
    fun validPayloadWithoutWorkplaceHasNullWorkplaceId() {
        val result = parseCheckInPayload("""{"tid":"1-234567890","typ":"popp-checkin"}""")
        val valid = assertIs<ScanResult.Valid>(result)
        assertEquals("1-234567890", valid.payload.telematikId)
        assertEquals(null, valid.payload.workplaceId)
    }

    @Test
    fun blankWorkplaceIdIsTreatedAsAbsent() {
        val result = parseCheckInPayload("""{"tid":"x","typ":"popp-checkin","wpid":"  "}""")
        val valid = assertIs<ScanResult.Valid>(result)
        assertEquals(null, valid.payload.workplaceId)
    }

    @Test
    fun fieldOrderDoesNotMatter() {
        val result = parseCheckInPayload("""{"typ":"popp-checkin","wpid":"R1","tid":"x"}""")
        assertIs<ScanResult.Valid>(result)
    }

    @Test
    fun wrongTypeIsRejected() {
        val result = parseCheckInPayload("""{"tid":"x","typ":"something-else"}""")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.WRONG_TYPE), result)
    }

    @Test
    fun missingTidIsRejected() {
        val result = parseCheckInPayload("""{"typ":"popp-checkin"}""")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.MISSING_TID), result)
    }

    @Test
    fun blankTidIsRejected() {
        val result = parseCheckInPayload("""{"tid":"   ","typ":"popp-checkin"}""")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.MISSING_TID), result)
    }

    @Test
    fun unknownFieldIsRejected() {
        val result = parseCheckInPayload("""{"tid":"x","typ":"popp-checkin","evil":"1"}""")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON), result)
    }

    @Test
    fun malformedJsonIsRejected() {
        val result = parseCheckInPayload("""{"tid":"x","typ":""")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON), result)
    }

    @Test
    fun emptyInputIsRejected() {
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON), parseCheckInPayload(""))
    }

    @Test
    fun bareUrlIsRejectedAndNeverResolved() {
        val result = parseCheckInPayload("https://test.example/checkin?tid=x")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON), result)
    }

    @Test
    fun jsonStringUrlIsRejected() {
        val result = parseCheckInPayload("\"https://test.example\"")
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_JSON), result)
    }

    @Test
    fun nonUtf8BytesAreRejected() {
        val result = parseCheckInPayload(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00))
        assertEquals(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_UTF8), result)
    }
}
