package de.servicehealth.poppmodule.sdk.federation.http

import io.ktor.client.HttpClient

internal expect fun createFederationHttpClient(): HttpClient
