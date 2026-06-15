package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.egk.protocol.PoppJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal actual fun createPoppWebSocketClient(trustedCaPem: String?): HttpClient =
    HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(PoppJson.instance)
        }
        engine {
            if (trustedCaPem != null) {
                https {
                    // DEV/TEST ONLY: trust exactly the given CA (the self-signed local docker
                    // ingress) — full chain validation still applies, unlike a trust-all manager.
                    trustManager = trustManagerFor(trustedCaPem)
                }
            }
        }
    }

private fun trustManagerFor(caPem: String): X509TrustManager {
    val certificate =
        CertificateFactory.getInstance("X.509")
            .generateCertificate(caPem.byteInputStream())
    val keyStore =
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("popp-trusted-ca", certificate)
        }
    val factory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(keyStore) }
    return factory.trustManagers.filterIsInstance<X509TrustManager>().first()
}
