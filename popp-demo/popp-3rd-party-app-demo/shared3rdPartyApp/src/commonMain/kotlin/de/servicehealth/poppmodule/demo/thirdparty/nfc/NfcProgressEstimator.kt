package de.servicehealth.poppmodule.demo.thirdparty.nfc

/**
 * Maps eGK read progress to a monotonic percentage for the scan UI.
 *
 * `EgkProgress` is emitted before every card transceive but the total number of scenarios is not
 * known up front, so an exact 0→100 % is not derivable. This is a deliberate heuristic: each
 * observed transceive nudges the percentage forward by [perStep], capped at [stepCap] (< 100).
 * 100 % is reserved for a confirmed [NfcScanUiState.Succeeded] and is set by the controller.
 */
class NfcProgressEstimator(
    private val stepCap: Int = 95,
    private val perStep: Int = 12,
) {
    private var percent = 0

    /** Advances and returns the new percentage (never decreases, never exceeds [stepCap]). */
    fun onStep(): Int {
        percent = (percent + perStep).coerceAtMost(stepCap)
        return percent
    }
}
