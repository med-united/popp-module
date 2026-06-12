package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage

/** Scripted in-memory transport: replays [inbound] in order and records what the client sent. */
internal class FakeTransport(inbound: List<PoppMessage>) : PoppServiceTransport {
    private val inbound = ArrayDeque(inbound)
    val sent = mutableListOf<PoppMessage>()
    var opened = false
        private set
    var closed = false
        private set

    override suspend fun open() {
        opened = true
    }

    override suspend fun send(message: PoppMessage) {
        sent += message
    }

    override suspend fun receive(): PoppMessage =
        inbound.removeFirstOrNull() ?: throw PoppSdkError.Network("FakeTransport: no more inbound messages")

    override suspend fun close() {
        closed = true
    }
}
