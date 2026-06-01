package de.servicehealth.poppmodule.sdk

/**
 * Public entry point of the PoPP SDK exposed to host apps
 * Placeholder API for the initial module setup
 */
class PoppSdk {

    fun version(): String = "popp-sdk $VERSION"

    fun platformInfo(): String = "Running on ${getPlatform().name}"

    companion object {
        const val VERSION: String = "0.0.1"
    }
}
