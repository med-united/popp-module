package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NfcOutcomeDispatcherTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun succeeded_fires_on_success() =
        runComposeUiTest {
            var token: String? = null
            setContent {
                NfcOutcomeDispatcher(
                    state = NfcScanUiState.Succeeded("jwt", "pn"),
                    onSuccess = { t, _ -> token = t },
                    onError = { _, _ -> },
                )
            }
            waitForIdle()
            assertEquals("jwt", token)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun failed_fires_on_error() =
        runComposeUiTest {
            var reason: NfcScanFailure? = null
            setContent {
                NfcOutcomeDispatcher(
                    state = NfcScanUiState.Failed(NfcScanFailure.SERVER_REJECTED, "x"),
                    onSuccess = { _, _ -> },
                    onError = { r, _ -> reason = r },
                )
            }
            waitForIdle()
            assertEquals(NfcScanFailure.SERVER_REJECTED, reason)
        }
}
