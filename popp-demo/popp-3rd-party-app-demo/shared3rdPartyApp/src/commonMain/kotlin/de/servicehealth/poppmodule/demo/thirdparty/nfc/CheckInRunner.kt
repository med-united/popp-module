package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.EgkCheckInResult
import de.servicehealth.poppmodule.sdk.egk.EgkProgress

/**
 * The check-in operation the controller drives. Production binding is `sdk::checkInWithEgk`;
 * tests inject a fake. Keeps [NfcCheckInController] independent of the concrete [PoppSdk].
 */
fun interface CheckInRunner {
    suspend fun run(
        channel: EgkApduChannel,
        onProgress: (EgkProgress) -> Unit,
    ): EgkCheckInResult
}
