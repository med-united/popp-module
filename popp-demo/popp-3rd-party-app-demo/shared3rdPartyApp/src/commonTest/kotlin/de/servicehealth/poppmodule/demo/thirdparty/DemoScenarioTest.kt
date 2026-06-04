package de.servicehealth.poppmodule.demo

import de.servicehealth.poppmodule.demo.model.demoScenarios
import kotlin.test.Test
import kotlin.test.assertEquals

class DemoScenarioTest {
    @Test
    fun hasThreeScenariosWithStableIds() {
        assertEquals(
            listOf("online_pharmacy", "telemedicine", "therapy"),
            demoScenarios.map { it.id },
        )
    }
}
