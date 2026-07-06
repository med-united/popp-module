package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel

/** A do-nothing channel — the fake runner ignores it; it only needs to exist. */
class FakeEgkApduChannel : EgkApduChannel {
    override suspend fun transceive(commandApduHex: String): String = "9000"
}
