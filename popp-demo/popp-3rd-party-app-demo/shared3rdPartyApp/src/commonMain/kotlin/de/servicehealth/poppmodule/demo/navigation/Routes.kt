package de.servicehealth.poppmodule.demo.navigation

object Routes {
    const val LAUNCHER = "popp_launcher"
    const val CHECK_IN_ENTRY = "check_in_entry"
    const val CHECK_IN_QR = "check_in_qr"
    const val CHECK_IN_CAN = "check_in_can"
    const val CHECK_IN_NFC = "check_in_nfc"
    const val ARG_SCENARIO = "scenario"
    const val INSTITUTION_SEARCH = "institution_search"
    const val INSURANCE_SELECTION = "insurance_selection"
    const val CHECK_IN_SUCCESS = "check_in_success"
    const val CHECK_IN_ERROR = "check_in_error"
    const val ARG_FAILURE = "failure"
    const val ARG_PROOF_TIME = "proofTime"

    const val CONFIRM_INSTITUTION = "confirm_institution"

    // Args for confirm institution screen
    const val ARG_INSTITUTION_ID = "institution_id"
    const val ARG_NAME = "name"
    const val ARG_ADDRESS = "address"
    const val ARG_CATEGORY = "category"

    const val CHECK_IN_SUCCESS_ROUTE = "$CHECK_IN_SUCCESS?$ARG_PROOF_TIME={$ARG_PROOF_TIME}"

    const val CONFIRM_INSTITUTION_ROUTE =
        "$CONFIRM_INSTITUTION?$ARG_INSTITUTION_ID={$ARG_INSTITUTION_ID}&$ARG_NAME={$ARG_NAME}&$ARG_ADDRESS={$ARG_ADDRESS}&$ARG_CATEGORY={$ARG_CATEGORY}"

    fun checkInSuccess(proofTimeEpochSeconds: Long?) =
        "$CHECK_IN_SUCCESS?$ARG_PROOF_TIME=${proofTimeEpochSeconds ?: ""}"

    fun checkInError(failure: String) = "$CHECK_IN_ERROR?$ARG_FAILURE=$failure"

    fun confirmInstitution(
        institutionId: String,
        name: String,
        address: String,
        category: String,
    ) =
        "$CONFIRM_INSTITUTION?$ARG_INSTITUTION_ID=${encode(institutionId)}&$ARG_NAME=${encode(name)}&$ARG_ADDRESS=${encode(address)}&$ARG_CATEGORY=${encode(category)}"

    private fun encode(s: String) = s.replace(" ", "%20").replace(",", "%2C")
}
