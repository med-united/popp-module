package de.servicehealth.poppmodule.sdk.egk.transport

import io.ktor.client.HttpClient

/**
 * Builds a Ktor [HttpClient] with the WebSockets plugin and the shared JSON content converter
 * installed, using each platform's engine (Android/JVM: CIO). The transport owns and closes the
 * returned client.
 *
 * @param trustedCaPem dev/test ONLY — PEM-encoded CA certificate to trust instead of the platform
 *   trust store, so the loop can reach the self-signed local docker ingress without disabling TLS
 *   validation. Must be null in production.
 */
internal expect fun createPoppWebSocketClient(trustedCaPem: String?): HttpClient
