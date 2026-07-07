package de.servicehealth.poppmodule.sdk.egk.transport

import de.gematik.zeta.sdk.ZetaSdkClient
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.PoppServiceTransport
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppJson
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch

/**
 * [PoppServiceTransport] that runs the eGK scenario loop over a ZETA-authenticated WebSocket.
 *
 * `ZetaSdkClient.ws(targetUrl) { … }` is a scoped callback that owns the connection for the
 * duration of its block; this class bridges it to the imperative open/send/receive/close seam by
 * running the block in [scope] and parking it on [closeSignal] until [close]. The block publishes
 * the live [DefaultClientWebSocketSession] via [opened]. ZETA's ws client has no Kotlinx WebSocket
 * converter, so frames are (de)serialized with [PoppJson] directly.
 *
 * **Single-use:** one connection per instance. Create a new instance for each connection attempt.
 */
internal class ZetaWsTransport(
    private val zeta: ZetaSdkClient,
    private val targetUrl: String,
    private val caPem: String? = null,
) : PoppServiceTransport {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val opened = CompletableDeferred<DefaultClientWebSocketSession>()
    private val closeSignal = CompletableDeferred<Unit>()
    private var session: DefaultClientWebSocketSession? = null

    override suspend fun open() {
        check(!opened.isCompleted) { "ZetaWsTransport is single-use — create a new instance per connection" }
        scope.launch {
            try {
                // DEV/TEST: caPem lets the ws leg trust the self-signed local ingress, mirroring the
                // main ZETA client's CA wiring in toZetaBuildConfig. Null in production (platform trust).
                zeta.ws(targetUrl = targetUrl, builder = { caPem?.let { addCaPem(it) } }) { // this: DefaultClientWebSocketSession
                    opened.complete(this)
                    closeSignal.await() // keep the ws scope alive until close()
                }
            } catch (e: CancellationException) {
                if (!opened.isCompleted) opened.completeExceptionally(e)
                throw e
            } catch (t: Throwable) {
                opened.completeExceptionally(PoppSdkError.Network("ZETA ws connect failed: $targetUrl", t))
            }
        }
        session = opened.await() // throws the classified error if connect failed
    }

    override suspend fun send(message: PoppMessage) {
        val active = session ?: throw PoppSdkError.Network("transport not open")
        try {
            active.send(Frame.Text(PoppJson.instance.encodeToString(PoppMessage.serializer(), message)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            throw PoppSdkError.Network("WebSocket send failed", e)
        }
    }

    override suspend fun receive(): PoppMessage {
        val active = session ?: throw PoppSdkError.Network("transport not open")
        val frame =
            try {
                active.incoming.receive()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClosedReceiveChannelException) {
                throw PoppSdkError.Network("WebSocket closed while awaiting a PoppMessage", e)
            } catch (e: Throwable) {
                throw PoppSdkError.Network("WebSocket receive failed", e)
            }
        val text =
            (frame as? Frame.Text)?.readText()
                ?: throw PoppSdkError.Protocol("expected a text frame, got ${frame.frameType}")
        return try {
            PoppJson.instance.decodeFromString(PoppMessage.serializer(), text)
        } catch (e: Throwable) {
            throw PoppSdkError.Protocol("failed to read PoppMessage from WebSocket", e)
        }
    }

    override suspend fun close() {
        try {
            session?.close()
        } catch (_: Throwable) {
            // best-effort close
        }
        closeSignal.complete(Unit)
        scope.cancel()
    }
}
