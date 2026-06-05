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
    const val ARG_SCENARIO = "scenario"

    fun integratedHome(scenario: String) = "$INTEGRATED_HOME?$ARG_SCENARIO=$scenario"
    fun appToAppHome(scenario: String) = "$APP_TO_APP_HOME?$ARG_SCENARIO=$scenario"
}
