package de.servicehealth.poppmodule.sdk.internal

import de.servicehealth.poppmodule.sdk.PoppSdkConfig
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.storage.SecureStorage

/**
 * Platform-specific bridge to the ZETA client. Implemented for real on Android
 * (delegates to `de.gematik.zeta:zeta-sdk`); stubbed on iOS until gematik
 * publishes a native variant.
 */
internal interface ZetaEngine {
    /** Performs ZETA client registration and attestation at the ZETA Guard. */
    suspend fun start()

    /** Reports current ZETA client status, or a human-readable diagnostic. */
    suspend fun status(): String
}

internal expect fun createZetaEngine(
    context: PoppSdkContext,
    config: PoppSdkConfig,
    storage: SecureStorage,
): ZetaEngine