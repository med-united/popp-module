package de.servicehealth.poppmodule.sdk.egk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PoppTokenTest {
    private val tokenWithProofTime =
        "header.eyJwYXRpZW50UHJvb2ZUaW1lIjoxNzgyMTI4MjQ5fQ.signature"

    @Test
    fun parses_patient_proof_time_from_the_payload() {
        val claims = parsePoppTokenClaims(tokenWithProofTime)
        assertEquals(1782128249L, claims?.patientProofTimeEpochSeconds)
    }

    @Test
    fun returns_null_for_a_token_without_a_payload_segment() {
        assertNull(parsePoppTokenClaims("not-a-jwt"))
    }

    @Test
    fun returns_null_when_the_payload_is_not_valid_base64_json() {
        assertNull(parsePoppTokenClaims("aaaa.@@@notbase64@@@.bbbb"))
    }

    @Test
    fun returns_null_when_the_proof_time_claim_is_absent() {
        assertNull(parsePoppTokenClaims("aaaa.eyJpYXQiOjF9.bbbb"))
    }
}
