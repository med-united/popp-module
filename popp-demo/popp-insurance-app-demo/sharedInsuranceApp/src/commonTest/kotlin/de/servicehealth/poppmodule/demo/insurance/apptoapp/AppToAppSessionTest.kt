package de.servicehealth.poppmodule.demo.insurance.apptoapp

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppToAppSessionTest {
    @BeforeTest
    fun setup() {
        AppToAppSession.clearRequest()
    }

    @Test
    fun testValidDeepLinkIsParsedCorrectly() {
        val validUrl = "https://popp.service-health.de/app-to-app/auth?client_id=demo-3rd-party-app&state=random_state_xyz&redirect_uri=app%3A%2F%2Fcallback"

        val result = AppToAppSession.handleDeepLink(validUrl)

        assertTrue(result, "Should return true for a valid URL")

        val request = AppToAppSession.currentRequest.value
        assertEquals("demo-3rd-party-app", request?.clientId)
        assertEquals("random_state_xyz", request?.state)
        assertEquals("app://callback", request?.redirectUri)
    }

    @Test
    fun testInvalidUrlPathIsRejected() {
        val invalidUrl = "https://popp.service-health.de/wrong-path?client_id=demo&state=123&redirect_uri=app://callback"

        val result = AppToAppSession.handleDeepLink(invalidUrl)

        assertFalse(result, "Should return false for a URL with an invalid path")
        assertNull(AppToAppSession.currentRequest.value)
    }

    @Test
    fun testMissingParametersAreRejected() {
        val missingRedirectUri = "https://popp.service-health.de/app-to-app/auth?client_id=demo-3rd-party-app&state=123"

        val result = AppToAppSession.handleDeepLink(missingRedirectUri)

        assertFalse(result, "Should return false for a URL with missing redirect_uri")
        assertNull(AppToAppSession.currentRequest.value)
    }

    @Test
    fun testClearRequestRemovesState() {
        val validUrl = "https://popp.service-health.de/app-to-app/auth?client_id=demo-3rd-party-app&state=123&redirect_uri=test"
        AppToAppSession.handleDeepLink(validUrl)

        AppToAppSession.clearRequest()

        assertNull(AppToAppSession.currentRequest.value)
    }
}
