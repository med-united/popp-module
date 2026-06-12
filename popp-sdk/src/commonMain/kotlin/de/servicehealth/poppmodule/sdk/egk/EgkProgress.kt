package de.servicehealth.poppmodule.sdk.egk

/**
 * Progress signal for the eGK NFC-scan UI (POPPM-161): running command [index] (0-based) of
 * [count] within scenario [scenario] (0-based). Emitted before each card transceive.
 */
data class EgkProgress(val scenario: Int, val index: Int, val count: Int)
