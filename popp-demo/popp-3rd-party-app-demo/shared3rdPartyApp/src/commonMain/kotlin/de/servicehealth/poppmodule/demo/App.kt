package de.servicehealth.poppmodule.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.servicehealth.poppmodule.demo.model.IntegrationMode
import de.servicehealth.poppmodule.demo.navigation.Routes
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInQrScannerScreen
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.application_title
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

/** Cross-platform entry point: a single BrandTheme wrapping the demo navigation graph. */
@Composable
fun App(poppSdk: PoppSdk) {
    BrandTheme {
        CompositionLocalProvider(LocalPoppSdk provides poppSdk) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Routes.LAUNCHER) {
                composable(Routes.LAUNCHER) {
                    PoppLauncherScreen(
                        onStartDemo = { _, mode ->
                            when (mode) {
                                IntegrationMode.INTEGRATED -> nav.navigate(Routes.CHECK_IN_ENTRY)
                                IntegrationMode.APP_TO_APP -> nav.navigate(Routes.INSURANCE_SELECTION)
                            }
                        },
                    )
                }
                composable(Routes.CHECK_IN_ENTRY) {
                    OnsiteCheckInEntryScreen(
                        onClose = { nav.popBackStack() },
                        onSearchClick = { nav.navigate(Routes.INSTITUTION_SEARCH) },
                        onQrScanClick = { nav.navigate(Routes.CHECK_IN_QR) },
                    )
                }
                composable(Routes.INSTITUTION_SEARCH) {
                    InstitutionSearchScreen({ nav.popBackStack() }, onBack = { nav.popBackStack() }, applicationTitle = stringResource(Res.string.application_title))
                }
                composable(Routes.CHECK_IN_QR) {
                    OnsiteCheckInQrScannerScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                    )
                }
                composable(Routes.INSURANCE_SELECTION) {
                    InsuranceSelectionScreen({ nav.popBackStack() }, onBack = { nav.popBackStack() }, applicationTitle = stringResource(Res.string.application_title))
                }
            }
        }
    }
}
