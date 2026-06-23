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
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import de.servicehealth.poppmodule.sdk.storage.createSecureStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Public entry point of the PoPP SDK exposed to host apps.
 *
 * All HTTP/WebSocket requests required by the TI 2.0 / PoPP flow (VZD search, eGK / GID
 * check-in, token retrieval, …) must go through the ZETA Guard proxy on the
 * device. This façade owns the lifecycle of the underlying ZETA clients and
 * exposes a small surface to host apps; under the hood it delegates to the
 * platform-specific [ZetaEngine].
 *
 * Two ZETA engine instances are managed internally:
 * - device engine: authenticates via device attestation only
 * - user engine: authenticates via the eGK or GesundheitsId credential
 */
class PoppSdk internal constructor(
    private val context: PoppSdkContext? = null,
    internal val storageOverride: SecureStorage? = null,
    private val engineFactory: (PoppSdkConfig, SecureStorage) -> ZetaEngine = ::createZetaEngine,
    private val transportFactory: (url: String, trustedCaPem: String?) -> PoppServiceTransport = ::defaultTransportFactory,
    private val trustedCaPem: String? = null,
    private val newSessionId: () -> String = ::defaultSessionId,
) {
    @Volatile
    private var configuredFqdn: String? = null

    private val deviceStorage by lazy {
        storageOverride ?: context?.let { createSecureStorage(it, DEVICE_STORAGE_NAMESPACE) }
    }

    private val userStorage by lazy {
        storageOverride ?: context?.let { createSecureStorage(it, USER_STORAGE_NAMESPACE) }
    }

    private val deviceMutex = Mutex()

    @Volatile
    private var deviceEngine: ZetaEngine? = null

    private val userMutex = Mutex()

    @Volatile
    private var userEngine: ZetaEngine? = null

    /** Current ZETA client status, as reported by the device engine. */
    suspend fun status(): String =
        ensureDeviceEngine().status()

    fun version(): String = "popp-sdk $VERSION"

    fun platformInfo(): String = getPlatform().name

    /**
     * Configures the FQDN of the PoPP service endpoint used by the ZETA clients.
     * Must be called once during app initialization, before any API calls.
     *
     * Spec ref: gemSpec_PoPP_Modul §3.3.4
     *
     * @param fqdn Fully Qualified Domain Name (including scheme and path)
     *   of the PoPP service, e.g.
     *   `wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc`
     */
    fun init(fqdn: String) {
        require(fqdn.isNotBlank()) { "fqdn must not be blank" }
        val previous = configuredFqdn
        if (previous != null) {
            if (previous == fqdn) return
            throw PoppSdkError.Configuration(
                "PoppSdk already initialised with $previous — re-initialisation with a different FQDN is not supported",
            )
        }
        configuredFqdn = fqdn
    }

    /** Smoke-tests connectivity to the PoPP service via the device ZETA engine. */
    suspend fun hello() {
        ensureDeviceEngine().hello()
    }

    /**
     * Runs the eGK scenario read loop (POPPM-118, gemSpec_PoPP_Modul §3.3.7) against the
     * PoPP-Service at the FQDN passed to [init] and returns a PoPP token.
     * Drives [channel] for each command APDU; reports [onProgress].
     *
     * Business failure (server `Error`) → [EgkCheckInResult.Failed]. Infrastructure failure (socket,
     * TLS, serialization, status-word mismatch, timeout) → [PoppSdkError].
     *
     * @throws PoppSdkError.Configuration if [init] was not called first.
     */
    suspend fun checkInWithEgk(
        channel: EgkApduChannel,
        onProgress: (EgkProgress) -> Unit = {},
    ): EgkCheckInResult {
        val fqdn =
            configuredFqdn
                ?: throw PoppSdkError.Configuration("PoppSdk not initialised — call init(fqdn) first")
        // Direct WebSocket transport for now. TODO(POPPM-119 follow-up): route through ZETA via
        // ensureDeviceEngine().scenarioTransport() once a real device / eGK (user) token exists — the
        // placeholder DeviceOnly token is rejected (400 "keine Berechtigung") on the eGK ws resource.
        val transport = transportFactory(fqdn, trustedCaPem)
        return EgkReadDriver(transport, channel, newSessionId).run(onProgress)
    }

    private suspend fun ensureDeviceEngine(): ZetaEngine {
        deviceEngine?.let { return it }
        return deviceMutex.withLock {
            deviceEngine ?: createDeviceEngine().also { deviceEngine = it }
        }
    }

    private suspend fun ensureUserEngine(): ZetaEngine {
        userEngine?.let { return it }
        return userMutex.withLock {
            userEngine ?: createUserEngine().also { userEngine = it }
        }
    }

    private suspend fun createDeviceEngine(): ZetaEngine {
        val fqdn =
            configuredFqdn
                ?: throw PoppSdkError.Configuration("PoppSdk not initialised — call init(fqdn) first")
        val storage =
            deviceStorage
                ?: throw PoppSdkError.Configuration("PoppSdk not initialised — call PoppSdk(context) first")

        val engineConfig =
            PoppSdkConfig(
                fqdn = fqdn,
                productId = PRODUCT_ID,
                productVersion = VERSION,
                clientName = "service-health-popp-module",
                platformIdentity =
                    PlatformIdentity.Android(
                        packageName = "de.servicehealth.poppmodule",
                        sha256CertFingerprints = listOf("AA:BB:CC"),
                    ),
                scopes = listOf("openid"),
                requiredRoleOid = REQUIRED_ROLE_OID,
                tokenProvider = DeviceOnly,
            )

        val e = engineFactory(engineConfig, storage)
        try {
            e.start()
        } catch (e: PoppSdkError) {
            throw e
        } catch (e: Throwable) {
            throw PoppSdkError.Unknown("Failed to start device ZETA engine", e)
        }
        return e
    }

    private suspend fun createUserEngine(): ZetaEngine {
        val fqdn =
            configuredFqdn
                ?: throw PoppSdkError.Configuration("PoppSdk not initialised — call init(fqdn) first")
        val storage =
            userStorage
                ?: throw PoppSdkError.Configuration("PoppSdk not initialised — call PoppSdk(context) first")
        val engineConfig =
            PoppSdkConfig(
                fqdn = fqdn,
                productId = PRODUCT_ID,
                productVersion = VERSION,
                clientName = "service-health-popp-module",
                platformIdentity =
                    PlatformIdentity.Android(
                        packageName = "de.servicehealth.poppmodule",
                        sha256CertFingerprints = listOf("AA:BB:CC"),
                    ),
                scopes = listOf("openid"),
                requiredRoleOid = REQUIRED_ROLE_OID,
                tokenProvider = TokenProviderConfig.Egk { error("eGK not yet implemented") },
            )
        val e = engineFactory(engineConfig, storage)
        try {
            e.start()
        } catch (e: PoppSdkError) {
            throw e
        } catch (e: Throwable) {
            throw PoppSdkError.Unknown("Failed to start user ZETA engine", e)
        }
        return e
    }

    companion object {
        operator fun invoke(context: PoppSdkContext? = null): PoppSdk = PoppSdk(context = context)

        private const val PRODUCT_ID = "de.servicehealth.popp"
        private const val REQUIRED_ROLE_OID = "1.2.276.0.76.4.156"
        private const val DEVICE_STORAGE_NAMESPACE = "popp-sdk-device"
        private const val USER_STORAGE_NAMESPACE = "popp-sdk-user"
        const val VERSION: String = "0.0.1"
    }
}

private fun defaultTransportFactory(
    url: String,
    trustedCaPem: String?,
): PoppServiceTransport =
    WebSocketScenarioTransport(createPoppWebSocketClient(trustedCaPem), url)

@OptIn(ExperimentalUuidApi::class)
private fun defaultSessionId(): String = Uuid.random().toString()
