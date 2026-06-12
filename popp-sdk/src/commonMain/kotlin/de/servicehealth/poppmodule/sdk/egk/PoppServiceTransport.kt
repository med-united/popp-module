package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.egk.protocol.PoppMessage

/**
 * The PoPP-Service wire, abstracted so the loop is transport-agnostic. The shipped implementation
 * is [de.servicehealth.poppmodule.sdk.egk.transport.WebSocketScenarioTransport]; a future
 * `ZetaHttpExecuteTransport` (the §3.3.7 `execute()` model) is a drop-in on the same seam.
 *
 * Implementations classify their own failures: socket/TLS → [PoppSdkError.Network][de.servicehealth.poppmodule.sdk.PoppSdkError.Network],
 * malformed frames → [PoppSdkError.Protocol][de.servicehealth.poppmodule.sdk.PoppSdkError.Protocol].
 */
internal interface PoppServiceTransport {
    suspend fun open()
    suspend fun send(message: PoppMessage)

    /** Suspends until the next inbound message. */
    suspend fun receive(): PoppMessage
    suspend fun close()
}
