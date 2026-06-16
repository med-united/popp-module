package de.servicehealth.poppmodule.demo.navigation

/**
 * Route names. `integrated_home` / `app_to_app_home` are named after the integration
 * mode they serve (the ticket's opaque "path_a/path_b" are deliberately not used).
 */
object Routes {
    const val LAUNCHER = "popp_launcher"
    const val INTEGRATED_HOME = "integrated_home"
    const val APP_TO_APP_HOME = "app_to_app_home"
    const val CHECK_IN_ENTRY = "check_in_entry"
    const val CHECK_IN_QR = "check_in_qr"
    const val CHECK_IN_CAN = "check_in_can"
    const val CHECK_IN_NFC = "check_in_nfc"
    const val ARG_SCENARIO = "scenario"
    const val INSTITUTION_SEARCH = "institution_search"
    const val CHECK_IN_SUCCESS = "check_in_success"
    const val CHECK_IN_ERROR = "check_in_error"
    const val ARG_FAILURE = "failure"

    fun checkInError(failure: String) = "$CHECK_IN_ERROR?$ARG_FAILURE=$failure"

    fun integratedHome(scenario: String) = "$INTEGRATED_HOME?$ARG_SCENARIO=$scenario"

    fun appToAppHome(scenario: String) = "$APP_TO_APP_HOME?$ARG_SCENARIO=$scenario"
}
