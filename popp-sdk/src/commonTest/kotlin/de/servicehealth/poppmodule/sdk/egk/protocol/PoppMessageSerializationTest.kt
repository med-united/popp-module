package de.servicehealth.poppmodule.sdk.egk.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PoppMessageSerializationTest {

    private val json = PoppJson.instance

    @Test
    fun start_message_round_trips_with_type_discriminator() {
        val msg: PoppMessage = StartMessage(clientSessionId = "sess-1")
        val text = json.encodeToString(PoppMessage.serializer(), msg)
        assertTrue(text.contains("\"type\":\"Start\""), "missing discriminator in: $text")
        assertTrue(text.contains("\"clientSessionId\":\"sess-1\""))
        assertTrue(text.contains("\"version\":\"1.0.0\""), "defaults must be encoded: $text")
        val back = json.decodeFromString(PoppMessage.serializer(), text)
        assertEquals(msg, back)
    }

    @Test
    fun standard_scenario_round_trips() {
        val msg: PoppMessage = StandardScenarioMessage(
            version = "1.0.0",
            clientSessionId = "sess-1",
            sequenceCounter = 3,
            timeSpan = 1500,
            steps = listOf(ScenarioStep("00A4040C", listOf("9000", "6283"))),
        )
        val back = json.decodeFromString(PoppMessage.serializer(), json.encodeToString(PoppMessage.serializer(), msg))
        assertEquals(msg, back)
    }

    @Test
    fun scenario_response_round_trips() {
        val msg: PoppMessage = ScenarioResponseMessage(steps = listOf("9000", "6A82"))
        val back = json.decodeFromString(PoppMessage.serializer(), json.encodeToString(PoppMessage.serializer(), msg))
        assertEquals(msg, back)
    }

    @Test
    fun token_message_round_trips() {
        val msg: PoppMessage = TokenMessage(token = "jwt.compact.value", pn = "pruefnachweis")
        val back = json.decodeFromString(PoppMessage.serializer(), json.encodeToString(PoppMessage.serializer(), msg))
        assertEquals(msg, back)
    }

    @Test
    fun error_message_round_trips_with_null_detail() {
        val msg: PoppMessage = ErrorMessage(errorCode = "egk_check_failed", errorDetail = null)
        val text = json.encodeToString(PoppMessage.serializer(), msg)
        assertTrue(!text.contains("errorDetail"), "explicitNulls=false should drop nulls: $text")
        val back = json.decodeFromString(PoppMessage.serializer(), text)
        assertEquals(msg, back)
    }

    @Test
    fun decoding_tolerates_unknown_fields() {
        val wire = """{"type":"Token","token":"t","pn":"p","serverOnlyFutureField":42}"""
        val back = json.decodeFromString(PoppMessage.serializer(), wire)
        val token = assertIs<TokenMessage>(back)
        assertEquals("t", token.token)
    }
}
