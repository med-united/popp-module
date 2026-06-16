package de.servicehealth.poppmodule.demo

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import de.servicehealth.poppmodule.sdk.PoppSdk
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavigationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun integratedModeStartOpensCheckInEntry() =
        runComposeUiTest {
            setContent { App(PoppSdk()) }
            // Select scenario; mode defaults to INTEGRATED.
            onNodeWithText("Online-Apotheke").performScrollTo().performClick()
            onNodeWithText("Starte die Demo").performClick()
            onNodeWithText("VOR-ORT-CHECK-IN").assertExists()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appToAppModeStartOpensCheckInEntry() =
        runComposeUiTest {
            setContent { App(PoppSdk()) }
            onNodeWithText("Online-Apotheke").performScrollTo().performClick()
            onNodeWithText("App-zu-App").performScrollTo().performClick() // switch mode
            onNodeWithText("Starte die Demo").performClick()
            onNodeWithText("VOR-ORT-CHECK-IN").assertExists()
        }
}
