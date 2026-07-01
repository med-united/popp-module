package de.servicehealth.poppmodule.sdk.federation.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun createFederationHttpClient(): HttpClient = HttpClient(Darwin)
