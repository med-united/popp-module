package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import de.servicehealth.poppmodule.theme.BrandTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h2000dp")
class NfcScanContentTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun waiting_shows_guide() =
        runComposeUiTest {
            mainClock.autoAdvance = false // the NFC ring animates forever
            setContent {
                BrandTheme {
                    NfcScanContent(
                        state = NfcScanUiState.WaitingForCard,
                        supported = true,
                        onBack = {},
                        onClose = {},
                    )
                }
            }
            onNodeWithTag("nfc_status").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reading_shows_percent() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                BrandTheme {
                    NfcScanContent(
                        state = NfcScanUiState.Reading(42),
                        supported = true,
                        onBack = {},
                        onClose = {},
                    )
                }
            }
            onNodeWithTag("nfc_percent").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun completed_shows_verified_and_hides_reading_pill() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                BrandTheme {
                    NfcScanContent(
                        state = NfcScanUiState.Succeeded("jwt", "pn"),
                        supported = true,
                        onBack = {},
                        onClose = {},
                    )
                }
            }
            onNodeWithTag("nfc_verified").assertIsDisplayed()
            onNodeWithTag("nfc_percent").assertDoesNotExist()
        }
}
