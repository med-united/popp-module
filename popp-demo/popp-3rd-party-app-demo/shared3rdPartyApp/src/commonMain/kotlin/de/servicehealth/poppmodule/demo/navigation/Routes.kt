package de.servicehealth.poppmodule.demo.navigation

/**
 * Route names. `integrated_home` / `app_to_app_home` are named after the integration
 * mode they serve (the ticket's opaque "path_a/path_b" are deliberately not used).
 */
object Routes {
    const val LAUNCHER = "popp_launcher"
    const val CHECK_IN_ENTRY = "check_in_entry"
    const val CHECK_IN_QR = "check_in_qr"
    const val ARG_SCENARIO = "scenario"
    const val INSTITUTION_SEARCH = "institution_search"
    const val INSURANCE_SELECTION = "insurance_selection"
}
