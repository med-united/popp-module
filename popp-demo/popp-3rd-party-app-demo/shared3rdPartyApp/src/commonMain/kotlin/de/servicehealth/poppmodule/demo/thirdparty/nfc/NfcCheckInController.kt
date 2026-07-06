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
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException

private const val COMPLETED_HOLD_MILLIS = 1000L

/**
 * Orchestrates the eGK scan: binds an [EgkChannelSource] to a [CheckInRunner] and exposes the
 * resulting [NfcScanUiState] as a [StateFlow]. Single-shot — the first card (or source error) is
 * consumed; later taps are ignored until [stop] (the reader is disabled on dispose).
 */
@OptIn(ExperimentalAtomicApi::class)
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

    private val consumed = AtomicBoolean(false)

    fun start(can: String) {
        if (started || consumed.load()) return
        started = true
        source.start(
            can = can,
            onCard = { channel ->
                if (consumed.compareAndSet(expectedValue = false, newValue = true)) {
                    runCheckIn(channel)
                }
            },
            onError = { error ->
                if (consumed.compareAndSet(expectedValue = false, newValue = true)) {
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: PoppSdkError) {
                    NfcScanUiState.Failed(e.toNfcScanFailure(), e.message)
                } catch (e: Exception) {
                    NfcScanUiState.Failed(NfcScanFailure.UNKNOWN, e.message)
                }
        }
    }

    fun stop() {
        started = false
        source.stop()
    }
}
