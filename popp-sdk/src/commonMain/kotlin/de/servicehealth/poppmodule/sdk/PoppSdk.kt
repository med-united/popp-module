package de.servicehealth.poppmodule.sdk

import de.servicehealth.poppmodule.sdk.egk.EgkApduChannel
import de.servicehealth.poppmodule.sdk.egk.EgkCheckInResult
import de.servicehealth.poppmodule.sdk.egk.EgkProgress
import de.servicehealth.poppmodule.sdk.egk.EgkReadDriver
import de.servicehealth.poppmodule.sdk.egk.PoppServiceTransport
import de.servicehealth.poppmodule.sdk.egk.transport.WebSocketScenarioTransport
import de.servicehealth.poppmodule.sdk.egk.transport.createPoppWebSocketClient
import de.servicehealth.poppmodule.sdk.internal.ZetaEngine
import de.servicehealth.poppmodule.sdk.internal.createZetaEngine
import de.servicehealth.poppmodule.sdk.storage.createSecureStorage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Public entry point of the PoPP SDK exposed to host apps.
 *
 * All HTTP/WebSocket requests required by the TI 2.0 / PoPP flow (VZD search, eGK / GID
 * check-in, token retrieval, …) must go through the ZETA Guard proxy on the
 * device. This façade owns the lifecycle of the underlying ZETA client and
 * exposes a small surface to host apps; under the hood it delegates to the
 * platform-specific [ZetaEngine] (real on Android, stubbed on iOS until
 * gematik publishes a native variant).
 */
class PoppSdk internal constructor(
    private val engine: ZetaEngine?,
    private val fqdn: String?,
    private val trustedCaPem: String?,
    private val transportFactory: (url: String, trustedCaPem: String?) -> PoppServiceTransport,
    private val newSessionId: () -> String,
) {
    constructor() : this(
        engine = null,
        fqdn = null,
        trustedCaPem = null,
        transportFactory = ::defaultTransportFactory,
        newSessionId = ::defaultSessionId,
    )

    /** Current ZETA client status, as reported by the underlying SDK. */
    suspend fun status(): String =
        engine?.status()
            ?: throw PoppSdkError.Configuration("PoppSdk not started — call PoppSdk.start() first")

    fun version(): String = "popp-sdk $VERSION"

    fun platformInfo(): String = getPlatform().name

    /**
     * Runs the eGK scenario read loop (POPPM-118, gemSpec_PoPP_Modul §3.3.7) against the
     * PoPP-Service at [PoppSdkConfig.fqdn] and returns a PoPP token. Requires a started SDK.
     * Drives [channel] for each command APDU; reports [onProgress].
     *
     * Business failure (server `Error`) → [EgkCheckInResult.Failed]. Infrastructure failure (socket,
     * TLS, serialization, status-word mismatch, timeout) → [PoppSdkError].
     *
     * @throws PoppSdkError.Configuration if the SDK is not started.
     */
    suspend fun checkInWithEgk(
        channel: EgkApduChannel,
        onProgress: (EgkProgress) -> Unit = {},
    ): EgkCheckInResult {
        if (engine == null || fqdn == null) {
            throw PoppSdkError.Configuration("PoppSdk not started — call PoppSdk.start() first")
        }
        val transport = transportFactory(fqdn, trustedCaPem)
        return EgkReadDriver(transport, channel, newSessionId).run(onProgress)
    }

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
            return PoppSdk(
                engine = engine,
                fqdn = config.fqdn,
                trustedCaPem = config.trustedCaPem,
                transportFactory = ::defaultTransportFactory,
                newSessionId = ::defaultSessionId,
            )
        }

        /**
         * DEV/TEST ONLY ([PoppDevTransport]): a PoppSdk that runs [checkInWithEgk] straight against
         * the PoPP-Service WebSocket at [fqdn], bypassing the ZETA Guard handshake. Reuses the real
         * transport — only registration/attestation is skipped. Production must use [start].
         *
         * RECONCILE ON POPPM-115 MERGE: POPPM-115 rewrites this class (new `PoppSdk(context)` +
         * fire-and-forget `init(fqdn)` ZETA lifecycle, device/user engines, `DeviceOnlyTokenProvider`)
         * and drops `checkInWithEgk`. When it lands, reconcile so a single started SDK both inits ZETA
         * and runs the eGK loop. Note the demo's `local` flavor FQDN split: 115 points `local` at the
         * ZETA ingress (`wss://popp-zeta-ingress:443/ws`) for `init`, but the eGK read loop can't use
         * the ZETA-gated ingress yet (HTTP 401) so it needs the direct `ws://…:8443/ws` — until a
         * ZETA-authenticated eGK transport exists, keep these two endpoints distinct.
         */
        @PoppDevTransport
        fun directTransport(
            fqdn: String,
            trustedCaPem: String? = null,
        ): PoppSdk =
            PoppSdk(
                engine = NoZetaEngine,
                fqdn = fqdn,
                trustedCaPem = trustedCaPem,
                transportFactory = ::defaultTransportFactory,
                newSessionId = ::defaultSessionId,
            )
    }
}

/** DEV/TEST no-op engine: satisfies the started-SDK guard for [PoppSdk.directTransport] without ZETA. */
internal object NoZetaEngine : ZetaEngine {
    override suspend fun start() = Unit

    override suspend fun status(): String = "DIRECT (no ZETA)"
}

private fun defaultTransportFactory(
    url: String,
    trustedCaPem: String?,
): PoppServiceTransport =
    WebSocketScenarioTransport(createPoppWebSocketClient(trustedCaPem), url)

@OptIn(ExperimentalUuidApi::class)
private fun defaultSessionId(): String = Uuid.random().toString()
