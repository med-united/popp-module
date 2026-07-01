package de.servicehealth.poppmodule.sdk.federation

import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FederationJwtDecoderTest {
    // header = base64url({"alg":"ES256"}) = eyJhbGciOiJFUzI1NiJ9
    // payload = base64url({"k":"v"}) without padding = eyJrIjoidiJ9
    private val validJwt = "eyJhbGciOiJFUzI1NiJ9.eyJrIjoidiJ9.fakesig"

    @Test
    fun valid_jwt_returns_decoded_payload_json() {
        assertEquals("""{"k":"v"}""", decodeJwtPayload(validJwt))
    }

    @Test
    fun leading_and_trailing_whitespace_is_tolerated() {
        assertEquals("""{"k":"v"}""", decodeJwtPayload("  $validJwt  "))
    }

    @Test
    fun two_segment_string_throws_protocol_error() {
        assertFailsWith<PoppSdkError.Protocol> { decodeJwtPayload("header.payload") }
    }

    @Test
    fun empty_string_throws_protocol_error() {
        assertFailsWith<PoppSdkError.Protocol> { decodeJwtPayload("") }
    }

    @Test
    fun payload_without_base64_padding_is_decoded_correctly() {
        val payloadJson = """{"idp_list_endpoint":"https://x.example/list"}"""
        assertEquals(payloadJson, decodeJwtPayload(buildJwt(payloadJson)))
    }

    @Test
    fun alg_none_throws_protocol_error() {
        val header = base64UrlEncode("""{"alg":"none"}""")
        val payload = base64UrlEncode("""{"k":"v"}""")
        assertFailsWith<PoppSdkError.Protocol> { decodeJwtPayload("$header.$payload.sig") }
    }

    @Test
    fun unknown_algorithm_throws_protocol_error() {
        val header = base64UrlEncode("""{"alg":"HS256"}""")
        val payload = base64UrlEncode("""{"k":"v"}""")
        assertFailsWith<PoppSdkError.Protocol> { decodeJwtPayload("$header.$payload.sig") }
    }
}

@OptIn(ExperimentalEncodingApi::class)
internal fun buildJwt(payloadJson: String): String {
    val header = base64UrlEncode("""{"alg":"ES256"}""")
    val payload = base64UrlEncode(payloadJson)
    return "$header.$payload.fakesig"
}

@OptIn(ExperimentalEncodingApi::class)
private fun base64UrlEncode(input: String): String =
    Base64.UrlSafe.encode(input.encodeToByteArray()).trimEnd('=')
