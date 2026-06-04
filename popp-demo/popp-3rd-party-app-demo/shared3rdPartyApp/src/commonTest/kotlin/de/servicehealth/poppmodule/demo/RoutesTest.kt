package de.servicehealth.poppmodule.demo

import de.servicehealth.poppmodule.demo.navigation.Routes
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesTest {
    @Test
    fun integratedHomeAppendsScenarioArgument() {
        assertEquals("integrated_home?scenario=online_pharmacy", Routes.integratedHome("online_pharmacy"))
    }

    @Test
    fun appToAppHomeAppendsScenarioArgument() {
        assertEquals("app_to_app_home?scenario=telemedicine", Routes.appToAppHome("telemedicine"))
    }
}
