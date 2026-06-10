package de.servicehealth.poppmodule.sdk.egk.nfc

import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.SecureMessaging
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CommandApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.ResponseApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.WrongCanException
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.establishTrustedChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * [EgkApduChannel] over a contactless eGK: establishes PACE with the [can] lazily on the
 * first transceive, then wraps every logical APDU from the PoPP-Service in secure
 * messaging (the spec's "unverändert" proxying applies at the logical level — the
 * contactless interface itself always requires the PACE channel).
 *
 * Not safe for concurrent [transceive] calls — the read loop (POPPM-118) serializes them.
 * After a failure the cached secure channel is dropped (its send-sequence counter may be
 * out of sync with the card), so a retry on the same instance re-runs PACE from scratch.
 */
@OptIn(ExperimentalStdlibApi::class)
internal class PaceSecuredChannel(
    private val channel: ICardChannel,
    private val can: String,
) : EgkApduChannel {

    private var secureMessaging: SecureMessaging? = null

    override suspend fun transceive(commandApduHex: String): String = withContext(Dispatchers.IO) {
        try {
            val sm = secureMessaging
                ?: SecureMessaging(channel.establishTrustedChannel(can)).also { secureMessaging = it }
            val command = parseCommandApdu(commandApduHex.hexToByteArray())
            sm.decrypt(checkedTransmit(sm.encrypt(command))).bytes.toHexString(HexFormat.UpperCase)
        } catch (e: PoppSdkError) {
            secureMessaging = null
            throw e
        } catch (e: CancellationException) {
            secureMessaging = null
            throw e
        } catch (e: WrongCanException) {
            throw PoppSdkError.Card(
                CardErrorReason.WRONG_CAN,
                "PACE mutual authentication failed — the CAN does not match this card",
                e,
            )
        } catch (e: IOException) {
            secureMessaging = null
            throw PoppSdkError.Card(
                CardErrorReason.CARD_LOST,
                "lost connection to the eGK during the NFC exchange",
                e,
            )
        } catch (e: Throwable) {
            secureMessaging = null
            throw PoppSdkError.Card(
                CardErrorReason.SECURE_CHANNEL_FAILED,
                "eGK secure channel failure: ${e.message}",
                e,
            )
        }
    }

    /**
     * Guards the secure-messaging transmit with the reader's capabilities so an
     * unsendable APDU fails with its actual cause instead of a low-level I/O error
     * that would be misread as "card lost".
     */
    @Suppress("MagicNumber")
    private fun checkedTransmit(command: CommandApdu): ResponseApdu {
        val bytes = command.bytes
        check(channel.isExtendedLengthSupported || bytes.size <= 5 || bytes[4] != 0.toByte()) {
            "secure-messaging APDU uses extended length, which this NFC reader does not support"
        }
        check(bytes.size <= channel.maxTransceiveLength) {
            "secure-messaging APDU of ${bytes.size} bytes exceeds the reader's " +
                "transceive limit of ${channel.maxTransceiveLength}"
        }
        return channel.transmit(command)
    }
}
