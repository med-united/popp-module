package de.servicehealth.poppmodule.demo

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.InternalRoborazziApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h808dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SharedPreviewScreenshotTest(
    private val testParameter: ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter,
) {
    // testRule reads tester.options() at instance-creation time.
    // defaultOptionsFromPlugin must be set inside @Parameters (which JUnit 4 runs before
    // instance creation) so the options are visible here.
    @get:Rule
    val testRule =
        run {
            val lifecycleOptions =
                tester.options().testLifecycleOptions
                    as ComposePreviewTester.Options.JUnit4TestLifecycleOptions
            lifecycleOptions.testRuleFactory(testParameter.composeTestRule)
        }

    @Test
    fun capturePreview() {
        tester.test(testParameter)
    }

    companion object {
        private val tester = AndroidComposePreviewTester()

        @OptIn(InternalRoborazziApi::class)
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews(): List<ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter> {
            ComposePreviewTester.defaultOptionsFromPlugin =
                ComposePreviewTester.Options(
                    scanOptions =
                        ComposePreviewTester.Options.ScanOptions(
                            packages = listOf("de.servicehealth.poppmodule"),
                            includePrivatePreviews = true,
                        ),
                )
            return tester.testParameters()
        }
    }
}
