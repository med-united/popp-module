package de.servicehealth.poppmodule.demo.thirdparty.nfc

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.nfc.NfcAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.EgkNfcChannel

/**
 * Android [EgkChannelSource]: drives [NfcAdapter] reader mode, hands each discovered tag + the CAN
 * to [EgkNfcChannel.fromTag], and surfaces tag/eGK errors. The host owns the NFC session lifetime.
 */
class NfcReaderEgkChannelSource(private val activity: Activity) : EgkChannelSource {
    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    override val isSupported: Boolean
        get() = adapter?.isEnabled == true

    override fun start(
        can: String,
        onCard: (EgkApduChannel) -> Unit,
        onError: (PoppSdkError) -> Unit,
    ) {
        val nfc =
            adapter?.takeIf { it.isEnabled } ?: return onError(
                PoppSdkError.PlatformUnsupported("NFC is not available or is turned off"),
            )
        nfc.enableReaderMode(
            activity,
            { tag ->
                // Reader callback runs on a binder thread; fromTag is cheap (PACE runs lazily on
                // the first transceive). The controller launches the read on its own coroutine.
                try {
                    onCard(EgkNfcChannel.fromTag(tag, can))
                } catch (e: PoppSdkError) {
                    onError(e)
                } catch (e: Exception) {
                    onError(PoppSdkError.Card(CardErrorReason.CARD_LOST, "NFC tag could not be read", e))
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null,
        )
    }

    override fun stop() {
        adapter?.disableReaderMode(activity)
    }
}

@Composable
actual fun rememberEgkChannelSource(): EgkChannelSource {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    return remember(activity) {
        activity?.let { NfcReaderEgkChannelSource(it) } ?: UnsupportedEgkChannelSource
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
