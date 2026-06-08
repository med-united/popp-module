package de.servicehealth.poppmodule.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import de.servicehealth.poppmodule.demo.navigation.Routes
import de.servicehealth.poppmodule.demo.ui.apptoapp.AppToAppHomeScreen
import de.servicehealth.poppmodule.demo.ui.integrated.IntegratedHomeScreen
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInQrScannerScreen
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme

/** Cross-platform entry point: a single BrandTheme wrapping the demo navigation graph. */
@Composable
fun App() {
    BrandTheme {
        CompositionLocalProvider(LocalPoppSdk provides PoppSdk()) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Routes.LAUNCHER) {
                composable(Routes.LAUNCHER) {
                    PoppLauncherScreen(
                        onStartDemo = { _, _ -> nav.navigate(Routes.CHECK_IN_ENTRY) },
                    )
                }
                composable(
                    route = "${Routes.INTEGRATED_HOME}?${Routes.ARG_SCENARIO}={${Routes.ARG_SCENARIO}}",
                    arguments = listOf(navArgument(Routes.ARG_SCENARIO) {
                        type = NavType.StringType
                        nullable = true
                    }),
                ) { entry ->
                    IntegratedHomeScreen(
                        scenarioId = entry.arguments?.read { getStringOrNull(Routes.ARG_SCENARIO) },
                    )
                }
                composable(
                    route = "${Routes.APP_TO_APP_HOME}?${Routes.ARG_SCENARIO}={${Routes.ARG_SCENARIO}}",
                    arguments = listOf(navArgument(Routes.ARG_SCENARIO) {
                        type = NavType.StringType
                        nullable = true
                    }),
                ) { entry ->
                    AppToAppHomeScreen(
                        scenarioId = entry.arguments?.read { getStringOrNull(Routes.ARG_SCENARIO) },
                    )
                }
                composable(Routes.CHECK_IN_ENTRY) {
                    OnsiteCheckInEntryScreen(
                        onClose = { nav.popBackStack() },
                        onSearchClick = {},
                        onQrScanClick = { nav.navigate(Routes.CHECK_IN_QR) },
                    )
                }
                composable(Routes.CHECK_IN_QR) {
                    OnsiteCheckInQrScannerScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                    )
                }
            }
        }
    }
}
