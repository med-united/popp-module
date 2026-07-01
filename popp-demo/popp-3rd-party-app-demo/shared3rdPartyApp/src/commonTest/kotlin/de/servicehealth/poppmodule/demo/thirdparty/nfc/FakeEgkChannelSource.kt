package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel

/** Delivers [channel] immediately on start, or [error] if set. */
class FakeEgkChannelSource(
    private val channel: EgkApduChannel = FakeEgkApduChannel(),
    private val error: PoppSdkError? = null,
) : EgkChannelSource {
    override val isSupported: Boolean = true
    var stopped = false
        private set

    override fun start(
        can: String,
        onCard: (EgkApduChannel) -> Unit,
        onError: (PoppSdkError) -> Unit,
    ) {
        val e = error
        if (e != null) onError(e) else onCard(channel)
    }

    override fun stop() {
        stopped = true
    }
}
