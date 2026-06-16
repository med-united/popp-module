package de.servicehealth.poppmodule.sdk.internal

import de.servicehealth.poppmodule.sdk.PoppSdkConfig
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.storage.SecureStorage

/**
 * iOS stub. `de.gematik.zeta:zeta-sdk:1.0.1` ships only Android + JVM variants
 * on Maven Central, so we surface a clear [PoppSdkError.PlatformUnsupported]
 * here rather than silently failing later in the call chain.
 */
private class UnsupportedZetaEngine : ZetaEngine {
    override suspend fun start(): Nothing =
        throw PoppSdkError.PlatformUnsupported(
            "ZETA SDK has no iOS variant yet (zeta-sdk 1.0.1 is Android/JVM only). " +
                "Track gematik/zeta-sdk for an iOS native release.",
        )

    override suspend fun status(): String = "iOS: zeta-sdk not yet available"
}

internal actual fun createZetaEngine(
    context: PoppSdkContext,
    config: PoppSdkConfig,
    storage: SecureStorage,
): ZetaEngine = UnsupportedZetaEngine()
