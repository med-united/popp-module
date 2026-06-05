package de.servicehealth.poppmodule.demo

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavigationTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun integratedModeNavigatesToIntegratedHomeCarryingScenario() = runComposeUiTest {
        setContent { App() }
        // Select scenario; mode defaults to INTEGRATED.
        onNodeWithText("Online-Apotheke").performScrollTo().performClick()
        onNodeWithText("Starte die Demo").performClick()
        onNodeWithText("Integrated — online_pharmacy (placeholder)").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appToAppModeNavigatesToAppToAppHomeCarryingScenario() = runComposeUiTest {
        setContent { App() }
        onNodeWithText("Online-Apotheke").performScrollTo().performClick()
        onNodeWithText("App-zu-App").performScrollTo().performClick() // switch mode
        onNodeWithText("Starte die Demo").performClick()
        onNodeWithText("App-to-App — online_pharmacy (placeholder)").assertExists()
    }
}
