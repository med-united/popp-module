package de.servicehealth.poppmodule.sdk.federation.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createFederationHttpClient(): HttpClient = HttpClient(OkHttp)
