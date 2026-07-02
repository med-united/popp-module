package de.servicehealth.poppmodule.demo

import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.captureRoboImage
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.theme.BrandTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h808dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenSnapshotTest {
    @Test
    fun poppLauncherScreen() =
        capture("PoppLauncherScreen") {
            BrandTheme {
                PoppLauncherScreen(onStartDemo = { _, _ -> })
            }
        }

    @Test
    fun onsiteCheckInEntryScreen() =
        capture("OnsiteCheckInEntryScreen") {
            BrandTheme {
                OnsiteCheckInEntryScreen(
                    onClose = {},
                    onSearchClick = {},
                    onQrScanClick = {},
                )
            }
        }

    // OnsiteCheckInQrScannerScreen omitted — requires a live camera stream.
    // Revisit when a fake CameraController stub is available.

    private fun capture(
        name: String,
        content: @Composable () -> Unit,
    ) = captureRoboImage("build/outputs/roborazzi/$name.png") { content() }
}
