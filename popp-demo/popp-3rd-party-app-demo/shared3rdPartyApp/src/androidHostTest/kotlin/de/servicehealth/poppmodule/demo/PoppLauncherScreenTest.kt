package de.servicehealth.poppmodule.demo

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import de.servicehealth.poppmodule.demo.model.IntegrationMode
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.theme.BrandTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PoppLauncherScreenTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersHeaderScenariosModesAndStart() = runComposeUiTest {
        setContent { BrandTheme { PoppLauncherScreen(onStartDemo = { _, _ -> }) } }
        onNodeWithText("PoPP-Modul Demo").assertExists()
        onNodeWithText("Online-Apotheke").assertExists()
        onNodeWithText("Telemedizin").assertExists()
        onNodeWithText("Therapie").assertExists()
        onNodeWithText("Voll integriert").assertExists()
        onNodeWithText("App-zu-App").assertExists()
        onNodeWithText("Starte die Demo").assertExists()
        // The active mode's description is shown (defaults to INTEGRATED).
        onNodeWithText("Das PoPP-Modul läuft nahtlos in der Host-App, kein App-Wechsel.").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun startIsGatedOnScenarioSelection() = runComposeUiTest {
        setContent { BrandTheme { PoppLauncherScreen(onStartDemo = { _, _ -> }) } }
        onNodeWithText("Starte die Demo").assertIsNotEnabled()
        // Scenario cards live inside a verticalScroll; scroll into view so the touch lands.
        onNodeWithText("Online-Apotheke").performScrollTo().performClick()
        onNodeWithText("Starte die Demo").assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun modeDefaultsToIntegratedTogglesAndIsCarried() = runComposeUiTest {
        var lastScenario: String? = null
        var lastMode: IntegrationMode? = null
        setContent {
            BrandTheme {
                PoppLauncherScreen(
                    onStartDemo = { s, m -> lastScenario = s; lastMode = m },
                )
            }
        }
        // Without touching the segmented control, select a scenario and start.
        // Scenario cards and the segmented control live inside a verticalScroll; scroll
        // each into view first so the injected touch lands on it (the start button is a
        // pinned bottomBar and is always on-screen).
        onNodeWithText("Telemedizin").performScrollTo().performClick()
        onNodeWithText("Starte die Demo").performClick()
        assertEquals("telemedicine", lastScenario)
        assertEquals(IntegrationMode.INTEGRATED, lastMode)

        // Toggle to App-zu-App and start again; scenario is carried, mode flips.
        onNodeWithText("App-zu-App").performScrollTo().performClick()
        onNodeWithText("Starte die Demo").performClick()
        assertEquals("telemedicine", lastScenario)
        assertEquals(IntegrationMode.APP_TO_APP, lastMode)
    }
}
