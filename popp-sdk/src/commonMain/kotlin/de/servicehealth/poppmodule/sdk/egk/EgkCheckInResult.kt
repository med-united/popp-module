package de.servicehealth.poppmodule.sdk.egk

/**
 * Terminal outcome of an eGK check-in ([de.servicehealth.poppmodule.sdk.PoppSdk.checkInWithEgk]).
 *
 * A *business* outcome — both arms are normal returns. Infrastructure failures (socket, TLS,
 * serialization, status-word mismatch, timeout) are thrown as
 * [de.servicehealth.poppmodule.sdk.PoppSdkError] instead.
 */
sealed interface EgkCheckInResult {
    /** The PoPP-Service issued a token. */
    data class Success(val poppToken: String, val pruefnachweis: String) : EgkCheckInResult

    /** The PoPP-Service ended the exchange with an error (e.g. the eGK check failed). */
    data class Failed(val code: String, val detail: String?) : EgkCheckInResult
}
