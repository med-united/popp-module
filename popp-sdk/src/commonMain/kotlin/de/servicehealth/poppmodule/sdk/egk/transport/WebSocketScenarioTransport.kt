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
import kotlinx.coroutines.withTimeoutOrNull

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
            // Ktor CIO's WebSocketReader wraps an IOException in a CancellationException when the
            // TCP connection drops mid-read. Detect that by inspecting the cause: a non-
            // CancellationException cause means transport failure, while real structured-concurrency
            // cancellation (the driver's receiveNext withTimeout, caller cancel) has no such cause
            // and must keep propagating as cancellation.
            val networkCause = e.cause?.takeIf { it !is CancellationException }
            if (networkCause != null) {
                throw PoppSdkError.Network("WebSocket receive failed", networkCause)
            }
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
            // Bound the close handshake so an unresponsive server can't hang the caller's coroutine.
            withTimeoutOrNull(CLOSE_TIMEOUT_MS) { session?.close() }
        } catch (_: Throwable) {
            // closing best-effort
        } finally {
            session = null
            client.close()
        }
    }

    private companion object {
        const val CLOSE_TIMEOUT_MS = 5_000L
    }
}
