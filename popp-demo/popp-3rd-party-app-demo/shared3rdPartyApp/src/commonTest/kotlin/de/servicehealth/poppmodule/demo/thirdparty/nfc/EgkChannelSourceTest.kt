package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EgkChannelSourceTest {
    @Test
    fun unsupported_source_reports_unsupported_and_errors_on_start() {
        assertFalse(UnsupportedEgkChannelSource.isSupported)
        var error: PoppSdkError? = null
        UnsupportedEgkChannelSource.start(
            can = "123456",
            onCard = { error("must not deliver a card") },
            onError = { error = it },
        )
        assertTrue(error is PoppSdkError.PlatformUnsupported)
    }
}
