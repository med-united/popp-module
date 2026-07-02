package de.servicehealth.poppmodule.demo

import de.servicehealth.poppmodule.demo.navigation.Routes
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesTest {
    @Test
    fun launcherRouteHasExpectedValue() {
        assertEquals("popp_launcher", Routes.LAUNCHER)
    }

    @Test
    fun insuranceSelectionRouteHasExpectedValue() {
        assertEquals("insurance_selection", Routes.INSURANCE_SELECTION)
    }
}
