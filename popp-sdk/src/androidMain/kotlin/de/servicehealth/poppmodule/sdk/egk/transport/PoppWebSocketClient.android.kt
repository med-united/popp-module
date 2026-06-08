package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.egk.protocol.PoppJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal actual fun createPoppWebSocketClient(disableTlsValidation: Boolean): HttpClient =
    HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(PoppJson.instance)
        }
        engine {
            if (disableTlsValidation) {
                https {
                    // DEV/TEST ONLY: the local docker ZETA ingress uses a self-signed cert.
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    }
                }
            }
        }
    }
