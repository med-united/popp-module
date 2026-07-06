package de.servicehealth.poppmodule.demo.insurance.apptoapp

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppToAppRequest(
    val clientId: String,
    val state: String,
    val redirectUri: String,
)

object AppToAppSession {
    private val _currentRequest = MutableStateFlow<AppToAppRequest?>(null)
    val currentRequest: StateFlow<AppToAppRequest?> = _currentRequest.asStateFlow()

    fun handleDeepLink(urlString: String): Boolean {
        return try {
            val url = Url(urlString)

            // Check if the path matches our app-to-app auth endpoint
            if (!url.encodedPath.endsWith("/app-to-app/auth")) return false

            val clientId = url.parameters["client_id"]
            val state = url.parameters["state"]
            val redirectUri = url.parameters["redirect_uri"]

            if (clientId != null && state != null && redirectUri != null) {
                // Verify client_id here against known RPs
                if (isValidClient(clientId)) {
                    _currentRequest.value = AppToAppRequest(clientId, state, redirectUri)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            // URL parsing failed
            false
        }
    }

    fun clearRequest() {
        _currentRequest.value = null
    }

    private fun isValidClient(clientId: String): Boolean {
        // Dummy validation for the demo
        return clientId == "demo-3rd-party-app" || clientId.isNotEmpty()
    }
}
