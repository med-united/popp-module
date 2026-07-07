package de.servicehealth.poppmodule.demo.thirdparty.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OidcSessionStore {
    private val _codeVerifier = MutableStateFlow<String?>(null)
    val codeVerifier: StateFlow<String?> = _codeVerifier.asStateFlow()

    private val _state = MutableStateFlow<String?>(null)
    val state: StateFlow<String?> = _state.asStateFlow()

    fun store(
        verifier: String,
        state: String,
    ) {
        _codeVerifier.value = verifier
        _state.value = state
    }

    fun clear() {
        _codeVerifier.value = null
        _state.value = null
    }
}
