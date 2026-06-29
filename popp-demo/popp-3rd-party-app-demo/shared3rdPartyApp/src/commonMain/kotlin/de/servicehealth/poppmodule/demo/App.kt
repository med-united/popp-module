package de.servicehealth.poppmodule.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import de.servicehealth.poppmodule.demo.navigation.Routes
import de.servicehealth.poppmodule.demo.thirdparty.ConfirmInstitutionScreen
import de.servicehealth.poppmodule.demo.thirdparty.InstitutionSearchScreen
import de.servicehealth.poppmodule.demo.thirdparty.icon
import de.servicehealth.poppmodule.demo.thirdparty.label
import de.servicehealth.poppmodule.demo.thirdparty.mockInstitutions
import de.servicehealth.poppmodule.demo.thirdparty.LeiData
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInQrScannerScreen
import de.servicehealth.poppmodule.demo.thirdparty.stubLeiData
import de.servicehealth.poppmodule.demo.ui.apptoapp.AppToAppHomeScreen
import de.servicehealth.poppmodule.demo.ui.integrated.IntegratedHomeScreen
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme

@Composable
fun App(poppSdk: PoppSdk) {
    BrandTheme {
        CompositionLocalProvider(LocalPoppSdk provides poppSdk) {
            val nav = rememberNavController()
            // In-memory for now — persisting to device storage is a separate step (POPPM-116 follow-up).
            var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
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
                        favorites = mockInstitutions.filter { it.id in favoriteIds },
                        onFavoriteClick = { id, name, address, category ->
                            nav.navigate(Routes.confirmInstitution(id, name, address, category))
                        },
                    )
                }
                composable(Routes.INSTITUTION_SEARCH) {
                    InstitutionSearchScreen(
                        onClose = { nav.popBackStack() },
                        onBack = { nav.popBackStack() },
                        onInstitutionSelected = { institution ->
                            nav.navigate(
                                Routes.confirmInstitution(
                                    institution.id,
                                    institution.name,
                                    institution.address,
                                    institution.type.label,
                                ),
                            )
                        },
                        favoriteIds = favoriteIds,
                    )
                }
                composable(Routes.CHECK_IN_QR) {
                    OnsiteCheckInQrScannerScreen(
                        onBack = { nav.popBackStack() },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                        onSuccess = { nav.navigate(Routes.CONFIRM_INSTITUTION) },
                    )
                }
                composable(
                    route = Routes.CONFIRM_INSTITUTION_ROUTE,
                    arguments =
                        listOf(
                            navArgument(Routes.ARG_INSTITUTION_ID) {
                                type = NavType.StringType
                                nullable = true
                            },
                            navArgument(Routes.ARG_NAME) {
                                type = NavType.StringType
                                nullable = true
                            },
                            navArgument(Routes.ARG_ADDRESS) {
                                type = NavType.StringType
                                nullable = true
                            },
                            navArgument(Routes.ARG_CATEGORY) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) { entry ->
                    val institutionId = entry.arguments?.getString(Routes.ARG_INSTITUTION_ID)
                    val name = entry.arguments?.getString(Routes.ARG_NAME) ?: stubLeiData.name
                    val address = entry.arguments?.getString(Routes.ARG_ADDRESS) ?: stubLeiData.address
                    val category = entry.arguments?.getString(Routes.ARG_CATEGORY) ?: stubLeiData.institutionType
                    val institution = institutionId?.let { id -> mockInstitutions.find { it.id == id } } ?: mockInstitutions.first()
                    ConfirmInstitutionScreen(
                        leiData =
                            LeiData(
                                institutionType = category,
                                institutionTypeIcon = institution.type.icon(),
                                name = name,
                                address = address,
                                openingHours = stubLeiData.openingHours,
                            ),
                        isFavorite = institution.id in favoriteIds,
                        onToggleFavorite = {
                            favoriteIds =
                                if (institution.id in favoriteIds) {
                                    favoriteIds - institution.id
                                } else {
                                    favoriteIds + institution.id
                                }
                        },
                        onConfirm = { /* TODO: navigate to auth flow */ },
                        onBack = { nav.popBackStack() },
                        onChooseOther = { nav.popBackStack(Routes.CHECK_IN_ENTRY, inclusive = false) },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                    )
                }
            }
        }
    }
}
