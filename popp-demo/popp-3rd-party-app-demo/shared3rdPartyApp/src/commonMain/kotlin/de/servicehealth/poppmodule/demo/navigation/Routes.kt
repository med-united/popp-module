package de.servicehealth.poppmodule.demo.navigation

object Routes {
    const val LAUNCHER = "popp_launcher"
    const val INTEGRATED_HOME = "integrated_home"
    const val APP_TO_APP_HOME = "app_to_app_home"
    const val CHECK_IN_ENTRY = "check_in_entry"
    const val CHECK_IN_QR = "check_in_qr"
    const val ARG_SCENARIO = "scenario"
    const val INSTITUTION_SEARCH = "institution_search"
    const val CONFIRM_INSTITUTION = "confirm_institution"

    // Args for confirm institution screen
    const val ARG_INSTITUTION_ID = "institution_id"
    const val ARG_NAME = "name"
    const val ARG_ADDRESS = "address"
    const val ARG_CATEGORY = "category"

    const val CONFIRM_INSTITUTION_ROUTE =
        "$CONFIRM_INSTITUTION?$ARG_INSTITUTION_ID={$ARG_INSTITUTION_ID}&$ARG_NAME={$ARG_NAME}&$ARG_ADDRESS={$ARG_ADDRESS}&$ARG_CATEGORY={$ARG_CATEGORY}"

    fun confirmInstitution(
        institutionId: String,
        name: String,
        address: String,
        category: String,
    ) =
        "$CONFIRM_INSTITUTION?$ARG_INSTITUTION_ID=${encode(institutionId)}&$ARG_NAME=${encode(name)}&$ARG_ADDRESS=${encode(address)}&$ARG_CATEGORY=${encode(category)}"

    fun integratedHome(scenario: String) = "$INTEGRATED_HOME?$ARG_SCENARIO=$scenario"

    fun appToAppHome(scenario: String) = "$APP_TO_APP_HOME?$ARG_SCENARIO=$scenario"

    private fun encode(s: String) = s.replace(" ", "%20").replace(",", "%2C")
}
