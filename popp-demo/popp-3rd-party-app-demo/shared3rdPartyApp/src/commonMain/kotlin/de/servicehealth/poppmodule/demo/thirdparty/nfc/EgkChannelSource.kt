package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.runtime.Composable
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel

/**
 * Platform seam that turns a presented eGK into an [EgkApduChannel]. Android wires
 * `NfcAdapter` reader mode → `EgkNfcChannel.fromTag`; other platforms are unsupported.
 */
interface EgkChannelSource {
    val isSupported: Boolean

    /** Begin listening. Calls [onCard] once a card yields a channel, or [onError] on failure. */
    fun start(
        can: String,
        onCard: (EgkApduChannel) -> Unit,
        onError: (PoppSdkError) -> Unit,
    )

    fun stop()
}

/** Used on iOS and on Android devices without (enabled) NFC. */
object UnsupportedEgkChannelSource : EgkChannelSource {
    override val isSupported: Boolean = false

    override fun start(
        can: String,
        onCard: (EgkApduChannel) -> Unit,
        onError: (PoppSdkError) -> Unit,
    ) {
        onError(PoppSdkError.PlatformUnsupported("NFC scanning is not available on this platform/device"))
    }

    override fun stop() = Unit
}

/** Provides the platform [EgkChannelSource] (Android: NFC reader mode; iOS: unsupported). */
@Composable
expect fun rememberEgkChannelSource(): EgkChannelSource
