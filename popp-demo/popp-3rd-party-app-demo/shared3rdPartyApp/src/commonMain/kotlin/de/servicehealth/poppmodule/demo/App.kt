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
import de.servicehealth.poppmodule.demo.model.IntegrationMode
import de.servicehealth.poppmodule.demo.navigation.Routes
import de.servicehealth.poppmodule.demo.thirdparty.ConfirmInstitutionScreen
import de.servicehealth.poppmodule.demo.thirdparty.InstitutionSearchScreen
import de.servicehealth.poppmodule.demo.thirdparty.LeiData
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInEntryScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInQrScannerScreen
import de.servicehealth.poppmodule.demo.thirdparty.OnsiteCheckInSuccessScreen
import de.servicehealth.poppmodule.demo.thirdparty.can.CanInputScreen
import de.servicehealth.poppmodule.demo.thirdparty.can.CanStore
import de.servicehealth.poppmodule.demo.thirdparty.can.InMemoryCanStore
import de.servicehealth.poppmodule.demo.thirdparty.can.LocalCanStore
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.application_title
import de.servicehealth.poppmodule.demo.thirdparty.icon
import de.servicehealth.poppmodule.demo.thirdparty.label
import de.servicehealth.poppmodule.demo.thirdparty.mockInstitutions
import de.servicehealth.poppmodule.demo.thirdparty.nfc.ErrorPlaceholderScreen
import de.servicehealth.poppmodule.demo.thirdparty.nfc.NfcScanScreen
import de.servicehealth.poppmodule.demo.thirdparty.stubLeiData
import de.servicehealth.poppmodule.demo.ui.launcher.PoppLauncherScreen
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.egk.parsePoppTokenClaims
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

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
            // In-memory for now — persisting to device storage is a separate step (POPPM-116 follow-up).
            var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
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
                        applicationTitle = stringResource(Res.string.application_title),
                    )
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
                        onSuccess = { poppToken, _ ->
                            val proofTime = parsePoppTokenClaims(poppToken)?.patientProofTimeEpochSeconds
                            nav.navigate(Routes.checkInSuccess(proofTime)) {
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
                composable(
                    route = Routes.CHECK_IN_SUCCESS_ROUTE,
                    arguments =
                        listOf(
                            navArgument(Routes.ARG_PROOF_TIME) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) { entry ->
                    val proofTime =
                        entry.arguments
                            ?.read { getStringOrNull(Routes.ARG_PROOF_TIME) }
                            ?.toLongOrNull()
                    OnsiteCheckInSuccessScreen(
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                        proofEpochSeconds = proofTime,
                    )
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
                        onConfirm = { nav.navigate(Routes.CHECK_IN_CAN) },
                        onBack = { nav.popBackStack() },
                        onChooseOther = { nav.popBackStack(Routes.CHECK_IN_ENTRY, inclusive = false) },
                        onClose = { nav.popBackStack(Routes.LAUNCHER, inclusive = false) },
                    )
                }
                composable(Routes.INSURANCE_SELECTION) {
                    InsuranceSelectionScreen(
                        onClose = { nav.popBackStack() },
                        onBack = { nav.popBackStack() },
                        applicationTitle = stringResource(Res.string.application_title),
                    )
                }
            }
        }
    }
}
