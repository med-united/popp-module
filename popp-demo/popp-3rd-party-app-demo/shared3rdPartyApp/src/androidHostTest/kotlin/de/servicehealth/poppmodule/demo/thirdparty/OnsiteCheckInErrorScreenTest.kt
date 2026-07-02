package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.servicehealth.poppmodule.demo.thirdparty.nfc.NfcScanFailure
import de.servicehealth.poppmodule.theme.BrandTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h2000dp")
class OnsiteCheckInErrorScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun wrong_can_primary_reenters_can_and_shows_no_code() =
        runComposeUiTest {
            var retried = false
            var reentered = false
            setContent {
                BrandTheme {
                    OnsiteCheckInErrorScreen(
                        failure = NfcScanFailure.WRONG_CAN,
                        onRetry = { retried = true },
                        onReenterCan = { reentered = true },
                        onClose = {},
                    )
                }
            }
            onNodeWithTag("error_title").assertIsDisplayed()
            onNodeWithTag("error_code").assertDoesNotExist()
            onNodeWithTag("error_primary").performClick()
            assertTrue(reentered, "WRONG_CAN primary should re-enter the CAN")
            assertFalse(retried)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun card_lost_primary_retries() =
        runComposeUiTest {
            var retried = false
            setContent {
                BrandTheme {
                    OnsiteCheckInErrorScreen(
                        failure = NfcScanFailure.CARD_LOST,
                        onRetry = { retried = true },
                        onReenterCan = {},
                        onClose = {},
                    )
                }
            }
            onNodeWithTag("error_primary").performClick()
            assertTrue(retried, "CARD_LOST primary should retry the scan")
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun server_rejected_shows_code_and_primary_closes() =
        runComposeUiTest {
            var closed = false
            setContent {
                BrandTheme {
                    OnsiteCheckInErrorScreen(
                        failure = NfcScanFailure.SERVER_REJECTED,
                        code = "WarningUnknownCertificates",
                        onRetry = {},
                        onReenterCan = {},
                        onClose = { closed = true },
                    )
                }
            }
            onNodeWithTag("error_code").assertIsDisplayed()
            onNodeWithTag("error_primary").performClick()
            assertTrue(closed, "SERVER_REJECTED primary should close (not retry the same card)")
        }
}
