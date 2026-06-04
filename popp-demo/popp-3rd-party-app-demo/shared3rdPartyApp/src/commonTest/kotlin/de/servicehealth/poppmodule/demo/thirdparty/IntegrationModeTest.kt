package de.servicehealth.poppmodule.demo

import de.servicehealth.poppmodule.demo.model.IntegrationMode
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationModeTest {
    @Test
    fun hasExactlyTwoModesInOrder() {
        assertEquals(listOf("INTEGRATED", "APP_TO_APP"), IntegrationMode.entries.map { it.name })
    }
}
