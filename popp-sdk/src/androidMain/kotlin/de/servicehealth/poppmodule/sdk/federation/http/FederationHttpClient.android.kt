package de.servicehealth.poppmodule.sdk.federation.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

internal actual fun createFederationHttpClient(): HttpClient = HttpClient(CIO)
