package de.servicehealth.poppmodule.demo.thirdparty.nfc

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.EgkCheckInResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

private const val COMPLETED_HOLD_MILLIS = 1000L

/**
 * Orchestrates the eGK scan: binds an [EgkChannelSource] to a [CheckInRunner] and exposes the
 * resulting [NfcScanUiState] as a [StateFlow]. Single-shot — the first card (or source error) is
 * consumed; later taps are ignored until [stop] (the reader is disabled on dispose).
 */
class NfcCheckInController(
    private val source: EgkChannelSource,
    private val runner: CheckInRunner,
    private val estimator: NfcProgressEstimator = NfcProgressEstimator(),
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<NfcScanUiState>(NfcScanUiState.WaitingForCard)
    val state: StateFlow<NfcScanUiState> = _state.asStateFlow()

    @Volatile
    private var started = false

    @Volatile
    private var consumed = false

    fun start(can: String) {
        if (started) return
        started = true
        source.start(
            can = can,
            onCard = { channel ->
                if (!consumed) {
                    consumed = true
                    runCheckIn(channel)
                }
            },
            onError = { error ->
                if (!consumed) {
                    consumed = true
                    _state.value = NfcScanUiState.Failed(error.toNfcScanFailure(), error.message)
                }
            },
        )
    }

    private fun runCheckIn(channel: EgkApduChannel) {
        _state.value = NfcScanUiState.Reading(0)
        scope.launch {
            _state.value =
                try {
                    when (
                        val result =
                            runner.run(channel) {
                                _state.value = NfcScanUiState.Reading(estimator.onStep())
                            }
                    ) {
                        is EgkCheckInResult.Success -> {
                            _state.value = NfcScanUiState.Reading(100)
                            delay(COMPLETED_HOLD_MILLIS)
                            NfcScanUiState.Succeeded(result.poppToken, result.pruefnachweis)
                        }

                        is EgkCheckInResult.Failed ->
                            NfcScanUiState.Failed(NfcScanFailure.SERVER_REJECTED, result.code)
                    }
                } catch (e: PoppSdkError) {
                    NfcScanUiState.Failed(e.toNfcScanFailure(), e.message)
                }
        }
    }

    fun stop() = source.stop()
}
