package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppDevTransport
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(PoppDevTransport::class)
class PoppSdkDirectTransportTest {
    @Test
    fun direct_transport_passes_config_guard_and_uses_real_transport() =
        runTest {
            // No ZETA engine, but fqdn is set -> the started-SDK guard must NOT fire.
            // The real WebSocket transport then tries a dead port -> Network, proving the bypass.
            val sdk = PoppSdk.directTransport("ws://localhost:1/ws")
            assertFailsWith<PoppSdkError.Network> {
                sdk.checkInWithEgk(channel = FakeCard())
            }
        }
}
