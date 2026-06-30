package de.servicehealth.poppmodule.demo.thirdparty.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

sealed class ParResult {
    data class Success(val requestUri: String, val expiresIn: Int) : ParResult()

    data class Error(val message: String) : ParResult()
}

class OidcParClient {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    fun close() {
        httpClient.close()
    }

    suspend fun pushAuthorizationRequest(
        parEndpoint: String,
        clientId: String,
        redirectUri: String,
        scope: String = "openid e-rezept",
    ): ParResult {
        return try {
            val codeVerifier = PkceGenerator.generateCodeVerifier()
            val codeChallenge = PkceGenerator.generateCodeChallenge(codeVerifier)

            OidcSessionStore.storeVerifier(codeVerifier)

            val response =
                httpClient.submitForm(
                    url = parEndpoint,
                    formParameters =
                        Parameters.build {
                            append("client_id", clientId)
                            append("response_type", "code")
                            append("scope", scope)
                            append("code_challenge", codeChallenge)
                            append("code_challenge_method", "S256")
                            append("redirect_uri", redirectUri)
                        },
                )

            if (response.status.isSuccess()) {
                val parResponse: ParResponse =
                    try {
                        response.body()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        return ParResult.Error("Unexpected response format: ${e.message}")
                    }
                ParResult.Success(parResponse.requestUri, parResponse.expiresIn)
            } else {
                val errorResponse: ParErrorResponse =
                    try {
                        response.body()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        ParErrorResponse("unknown_error", response.status.description)
                    }
                val description = errorResponse.errorDescription?.let { " - $it" } ?: ""
                ParResult.Error("PAR failed: ${errorResponse.error}$description")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ParResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Completes the PKCE flow by exchanging the authorization code for tokens.
     * Call this from the deep-link callback handler when the insurance app redirects back
     * to `redirectUri?code=<authCode>`.
     */
    suspend fun exchangeCodeForToken(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        authCode: String,
    ): ParResult {
        val codeVerifier =
            OidcSessionStore.codeVerifier.value
                ?: return ParResult.Error("No code_verifier in session — PAR flow was not started")

        return try {
            val response =
                httpClient.submitForm(
                    url = tokenEndpoint,
                    formParameters =
                        Parameters.build {
                            append("grant_type", "authorization_code")
                            append("code", authCode)
                            append("client_id", clientId)
                            append("redirect_uri", redirectUri)
                            append("code_verifier", codeVerifier)
                        },
                )

            if (response.status.isSuccess()) {
                OidcSessionStore.clear()
                ParResult.Success("token_exchange_ok", 0)
            } else {
                val errorResponse: ParErrorResponse =
                    try {
                        response.body()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        ParErrorResponse("unknown_error", response.status.description)
                    }
                val description = errorResponse.errorDescription?.let { " - $it" } ?: ""
                ParResult.Error("Token exchange failed: ${errorResponse.error}$description")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ParResult.Error("Network error: ${e.message}")
        }
    }
}
