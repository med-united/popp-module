package de.servicehealth.poppmodule.sdk.egk.transport

import io.ktor.client.HttpClient

/**
 * Builds a Ktor [HttpClient] with the WebSockets plugin and the shared JSON content converter
 * installed, using each platform's engine (Android/JVM: CIO). The transport owns and closes the
 * returned client.
 *
 * @param disableTlsValidation dev/test ONLY — trusts any server certificate so the loop can reach
 *   the self-signed local docker ingress. Must be false in production.
 */
internal expect fun createPoppWebSocketClient(disableTlsValidation: Boolean): HttpClient
