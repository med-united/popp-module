package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_card_lost_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_card_lost_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_network_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_network_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_protocol_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_protocol_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_secure_channel_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_secure_channel_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_server_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_server_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_unknown_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_unknown_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_wrong_can_message
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_wrong_can_title
import org.jetbrains.compose.resources.StringResource

enum class NfcErrorRecovery { RETRY, REENTER_CAN, CLOSE }

data class NfcErrorUi(
    val titleRes: StringResource,
    val messageRes: StringResource,
    val primary: NfcErrorRecovery,
    val secondary: NfcErrorRecovery?,
)

fun NfcScanFailure.toErrorUi(): NfcErrorUi =
    when (this) {
        NfcScanFailure.WRONG_CAN ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_wrong_can_title,
                messageRes = Res.string.checkin_error_wrong_can_message,
                primary = NfcErrorRecovery.REENTER_CAN,
                secondary = NfcErrorRecovery.CLOSE,
            )

        NfcScanFailure.CARD_LOST ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_card_lost_title,
                messageRes = Res.string.checkin_error_card_lost_message,
                primary = NfcErrorRecovery.RETRY,
                secondary = NfcErrorRecovery.CLOSE,
            )

        NfcScanFailure.SECURE_CHANNEL ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_secure_channel_title,
                messageRes = Res.string.checkin_error_secure_channel_message,
                primary = NfcErrorRecovery.RETRY,
                secondary = NfcErrorRecovery.REENTER_CAN,
            )

        NfcScanFailure.NETWORK ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_network_title,
                messageRes = Res.string.checkin_error_network_message,
                primary = NfcErrorRecovery.RETRY,
                secondary = NfcErrorRecovery.CLOSE,
            )

        NfcScanFailure.SERVER_REJECTED ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_server_title,
                messageRes = Res.string.checkin_error_server_message,
                primary = NfcErrorRecovery.CLOSE,
                secondary = NfcErrorRecovery.RETRY,
            )

        NfcScanFailure.PROTOCOL ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_protocol_title,
                messageRes = Res.string.checkin_error_protocol_message,
                primary = NfcErrorRecovery.RETRY,
                secondary = NfcErrorRecovery.CLOSE,
            )

        NfcScanFailure.UNKNOWN ->
            NfcErrorUi(
                titleRes = Res.string.checkin_error_unknown_title,
                messageRes = Res.string.checkin_error_unknown_message,
                primary = NfcErrorRecovery.RETRY,
                secondary = NfcErrorRecovery.CLOSE,
            )
    }
