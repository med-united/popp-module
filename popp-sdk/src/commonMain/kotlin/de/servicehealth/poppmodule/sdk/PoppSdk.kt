package de.servicehealth.poppmodule.sdk

import de.servicehealth.poppmodule.sdk.internal.ZetaEngine
import de.servicehealth.poppmodule.sdk.internal.createZetaEngine
import de.servicehealth.poppmodule.sdk.storage.createSecureStorage

/**
 * Public entry point of the PoPP SDK exposed to host apps.
 *
 * All HTTP requests required by the TI 2.0 / PoPP flow (VZD search, eGK / GID
 * check-in, token retrieval, …) must go through the ZETA Guard proxy on the
 * device. This façade owns the lifecycle of the underlying ZETA client and
 * exposes a small surface to host apps; under the hood it delegates to the
 * platform-specific [ZetaEngine] (real on Android, stubbed on iOS until
 * gematik publishes a native variant).
 */
class PoppSdk private constructor(
    private val engine: ZetaEngine?,
) {

    constructor() : this(null)

    /** Current ZETA client status, as reported by the underlying SDK. */
    suspend fun status(): String = engine?.status()
        ?: throw PoppSdkError.Configuration("PoppSdk not started — call PoppSdk.start() first")

    fun version(): String = "popp-sdk $VERSION"

    fun platformInfo(): String = getPlatform().name

    companion object {
        private const val STORAGE_NAMESPACE = "popp-sdk"
        const val VERSION: String = "0.0.1"

        /**
         * Bootstraps the SDK against the ZETA Guard. Triggers the registration
         * and attestation handshake required by the spec, then returns a
         * usable [PoppSdk] instance.
         *
         * The call is `suspend` and is automatically exposed to Swift as
         * `async`/`await` via Kotlin/Native's Obj-C interop on iOS.
         *
         * @throws PoppSdkError on any ZETA-, network- or configuration-level
         *   failure. iOS callers currently always receive
         *   [PoppSdkError.PlatformUnsupported] — see [ZetaEngine].
         */
        suspend fun start(
            context: PoppSdkContext,
            config: PoppSdkConfig,
        ): PoppSdk {
            val storage = createSecureStorage(context, STORAGE_NAMESPACE)
            val engine = createZetaEngine(context, config, storage)
            try {
                engine.start()
            } catch (e: PoppSdkError) {
                throw e
            } catch (e: Throwable) {
                throw PoppSdkError.Unknown("Failed to start ZETA client", e)
            }
            return PoppSdk(engine)
        }
    }
}