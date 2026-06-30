package de.servicehealth.poppmodule.demo.thirdparty.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple in-memory session store for the demo to keep track of the `code_verifier`
 * and `state` across the App-to-App jump.
 */
object OidcSessionStore {
    private val _codeVerifier = MutableStateFlow<String?>(null)
    val codeVerifier: StateFlow<String?> = _codeVerifier.asStateFlow()

    fun storeVerifier(verifier: String) {
        _codeVerifier.value = verifier
    }

    fun clear() {
        _codeVerifier.value = null
    }
}
