package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.PoppServiceTransport
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException

/**
 * Real [PoppServiceTransport] over a Ktor client WebSocket. Connects to [url], serializes outbound
 * [PoppMessage]s to text frames and deserializes inbound ones via the JSON content converter
 * installed on [client]. Owns [client] and closes it on [close].
 *
 * Failure classification: connect/send/receive socket errors → [PoppSdkError.Network]; a frame that
 * can't be deserialized into a [PoppMessage] → [PoppSdkError.Protocol].
 */
internal class WebSocketScenarioTransport(
    private val client: HttpClient,
    private val url: String,
) : PoppServiceTransport {

    private var session: DefaultClientWebSocketSession? = null

    override suspend fun open() {
        session = try {
            client.webSocketSession(urlString = url)
        } catch (e: CancellationException) {
            throw e // never swallow structured-concurrency cancellation as an SDK error
        } catch (e: Throwable) {
            client.close()
            throw PoppSdkError.Network("WebSocket connect failed: $url", e)
        }
    }

    override suspend fun send(message: PoppMessage) {
        val active = session ?: throw PoppSdkError.Network("transport not open")
        try {
            active.sendSerialized<PoppMessage>(message)
        } catch (e: CancellationException) {
            throw e // never swallow structured-concurrency cancellation as an SDK error
        } catch (e: Throwable) {
            throw PoppSdkError.Network("WebSocket send failed", e)
        }
    }

    override suspend fun receive(): PoppMessage {
        val active = session ?: throw PoppSdkError.Network("transport not open")
        return try {
            active.receiveDeserialized<PoppMessage>()
        } catch (e: PoppSdkError) {
            throw e
        } catch (e: CancellationException) {
            // A timeout/cancellation (e.g. the driver's receiveNext withTimeout) must propagate as
            // cancellation, not be reclassified as a Protocol/Network failure.
            throw e
        } catch (e: ClosedReceiveChannelException) {
            // The server closed the socket before/while sending the next frame → transport failure.
            throw PoppSdkError.Network("WebSocket closed while awaiting a PoppMessage", e)
        } catch (e: Throwable) {
            // A frame arrived but couldn't be turned into a PoppMessage (malformed/unknown JSON):
            // the converter throws here → protocol violation, not a transport failure.
            throw PoppSdkError.Protocol("failed to read PoppMessage from WebSocket", e)
        }
    }

    override suspend fun close() {
        try {
            session?.close()
        } catch (_: Throwable) {
            // closing best-effort
        } finally {
            session = null
            client.close()
        }
    }
}
