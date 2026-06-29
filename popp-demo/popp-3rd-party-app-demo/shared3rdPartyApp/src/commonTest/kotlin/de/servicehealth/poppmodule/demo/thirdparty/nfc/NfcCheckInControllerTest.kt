package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.CardErrorReason
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.EgkCheckInResult
import de.servicehealth.poppmodule.sdk.egk.EgkProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class NfcCheckInControllerTest {
    @Test
    fun success_path_emits_succeeded() =
        runTest {
            val source = FakeEgkChannelSource()
            val runner =
                CheckInRunner { _, onProgress ->
                    onProgress(EgkProgress(0, 0, 2))
                    onProgress(EgkProgress(0, 1, 2))
                    EgkCheckInResult.Success("jwt", "pn")
                }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            runCurrent()

            assertEquals(NfcScanUiState.Reading(100), controller.state.value)

            advanceTimeBy(2.seconds)
            runCurrent()
            assertEquals(NfcScanUiState.Succeeded("jwt", "pn"), controller.state.value)
        }

    @Test
    fun server_failure_maps_to_server_rejected() =
        runTest {
            val source = FakeEgkChannelSource()
            val runner = CheckInRunner { _, _ -> EgkCheckInResult.Failed("errorCode", "UnknownCertificates") }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            runCurrent()

            assertEquals(
                NfcScanUiState.Failed(NfcScanFailure.SERVER_REJECTED, "UnknownCertificates"),
                controller.state.value,
            )
        }

    @Test
    fun sdk_error_maps_via_taxonomy() =
        runTest {
            val source = FakeEgkChannelSource()
            val runner =
                CheckInRunner { _, _ ->
                    throw PoppSdkError.Card(CardErrorReason.WRONG_CAN, "PACE failed")
                }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            runCurrent()

            val state = controller.state.value
            assertIs<NfcScanUiState.Failed>(state)
            assertEquals(NfcScanFailure.WRONG_CAN, state.reason)
        }

    @Test
    fun source_error_is_surfaced() =
        runTest {
            val source = FakeEgkChannelSource(error = PoppSdkError.PlatformUnsupported("no NFC"))
            val runner = CheckInRunner { _, _ -> error("runner must not be called") }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            runCurrent()

            assertEquals(
                NfcScanUiState.Failed(NfcScanFailure.UNKNOWN, "no NFC"),
                controller.state.value,
            )
        }

    @Test
    fun second_card_is_ignored() =
        runTest {
            var captured: ((EgkApduChannel) -> Unit)? = null
            val source =
                object : EgkChannelSource {
                    override val isSupported: Boolean = true

                    override fun start(
                        can: String,
                        onCard: (EgkApduChannel) -> Unit,
                        onError: (PoppSdkError) -> Unit,
                    ) {
                        captured = onCard
                    }

                    override fun stop() = Unit
                }
            var runs = 0
            val runner =
                CheckInRunner { _, _ ->
                    runs += 1
                    EgkCheckInResult.Success("jwt", "pn")
                }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            val channel = FakeEgkApduChannel()
            captured!!(channel)
            captured!!(channel) // a second tap must be ignored
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()

            assertEquals(1, runs)
            assertEquals(NfcScanUiState.Succeeded("jwt", "pn"), controller.state.value)
        }

    @Test
    fun re_arms_after_stop_until_a_card_is_consumed() =
        runTest {
            var startCount = 0
            var captured: ((EgkApduChannel) -> Unit)? = null
            val source =
                object : EgkChannelSource {
                    override val isSupported: Boolean = true

                    override fun start(
                        can: String,
                        onCard: (EgkApduChannel) -> Unit,
                        onError: (PoppSdkError) -> Unit,
                    ) {
                        startCount += 1
                        captured = onCard
                    }

                    override fun stop() = Unit
                }
            val runner = CheckInRunner { _, _ -> EgkCheckInResult.Success("jwt", "pn") }
            val controller = NfcCheckInController(source, runner, scope = backgroundScope)

            controller.start("123456")
            controller.start("123456")
            assertEquals(1, startCount)

            controller.stop()
            controller.start("123456")
            assertEquals(2, startCount)

            captured!!(FakeEgkApduChannel())
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()

            controller.stop()
            controller.start("123456")
            assertEquals(2, startCount)
        }
}
