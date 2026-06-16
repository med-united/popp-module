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
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInQrScannerScreen
import de.servicehealth.poppmodule.demo.thirdparty.can.CanInputScreen
import de.servicehealth.poppmodule.demo.thirdparty.can.CanStore
import de.servicehealth.poppmodule.demo.thirdparty.can.InMemoryCanStore
import de.servicehealth.poppmodule.demo.thirdparty.can.LocalCanStore
import de.servicehealth.poppmodule.demo.thirdparty.nfc.ErrorPlaceholderScreen
import de.servicehealth.poppmodule.demo.thirdparty.nfc.NfcScanScreen
import de.servicehealth.poppmodule.demo.thirdparty.nfc.SuccessPlaceholderScreen
import de.servicehealth.poppmodule.demo.ui.apptoapp.AppToAppHomeScreen
import de.servicehealth.poppmodule.demo.ui.integrated.IntegratedHomeScreen
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme

/** Cross-platform entry point: a single BrandTheme wrapping the demo navigation graph. */
@Composable
fun App(
    poppSdk: PoppSdk = PoppSdk(),
    canStore: CanStore = InMemoryCanStore(),
) {
    BrandTheme {
        CompositionLocalProvider(
            LocalPoppSdk provides poppSdk,
            LocalCanStore provides canStore,
        ) {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = Routes.LAUNCHER) {
                composable(Routes.LAUNCHER) {
                    PoppLauncherScreen(
                        onStartDemo = { _, _ -> nav.navigate(Routes.CHECK_IN_ENTRY) },
                    )
                }
                composable(
                    route = "${Routes.INTEGRATED_HOME}?${Routes.ARG_SCENARIO}={${Routes.ARG_SCENARIO}}",
                    arguments =
                        listOf(
                            navArgument(Routes.ARG_SCENARIO) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) { entry ->
                    IntegratedHomeScreen(
                        scenarioId = entry.arguments?.read { getStringOrNull(Routes.ARG_SCENARIO) },
                        onNavigateToSearch = { nav.navigate(Routes.INSTITUTION_SEARCH) },
                    )
                }
                composable(
                    route = "${Routes.APP_TO_APP_HOME}?${Routes.ARG_SCENARIO}={${Routes.ARG_SCENARIO}}",
                    arguments =
                        listOf(
                            navArgument(Routes.ARG_SCENARIO) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) { entry ->
                    AppToAppHomeScreen(
                        scenarioId = entry.arguments?.read { getStringOrNull(Routes.ARG_SCENARIO) },
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
                    InstitutionSearchScreen({ nav.popBackStack() }, onBack = { nav.popBackStack() })
                }
                composable(Routes.CHECK_IN_QR) {
                    OnsiteCheckInQrScannerScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                        onProceed = { nav.navigate(Routes.CHECK_IN_CAN) },
                    )
                }
                composable(Routes.CHECK_IN_CAN) {
                    CanInputScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                        onComplete = { nav.navigate(Routes.CHECK_IN_NFC) },
                    )
                }
                composable(Routes.CHECK_IN_NFC) {
                    NfcScanScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                        onSuccess = { _, _ ->
                            nav.navigate(Routes.CHECK_IN_SUCCESS) {
                                popUpTo(Routes.CHECK_IN_NFC) { inclusive = true }
                            }
                        },
                        onError = { reason, _ ->
                            nav.navigate(Routes.checkInError(reason.name)) {
                                popUpTo(Routes.CHECK_IN_NFC) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.CHECK_IN_SUCCESS) {
                    SuccessPlaceholderScreen(onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) })
                }
                composable(
                    route = "${Routes.CHECK_IN_ERROR}?${Routes.ARG_FAILURE}={${Routes.ARG_FAILURE}}",
                    arguments =
                        listOf(
                            navArgument(Routes.ARG_FAILURE) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) { entry ->
                    ErrorPlaceholderScreen(
                        failure = entry.arguments?.read { getStringOrNull(Routes.ARG_FAILURE) },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                    )
                }
            }
        }
    }
}
