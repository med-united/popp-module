package de.servicehealth.poppmodule.sdk

/**
 * Configuration passed to the underlying ZETA engines. Carries everything the
 * ZETA client needs to register, attest, and authenticate at the ZETA Guard.
 *
 * This is an internal type.
 */
internal data class PoppSdkConfig(
    /**
     * Fully Qualified Domain Name (including scheme and path) of the PoPP service, e.g.
     * `wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc`.
     * Used both as the ZETA resource and as the WebSocket "scenario" endpoint for
     * [PoppSdk.checkInWithEgk].
     */
    val fqdn: String,
    val productId: String,
    val productVersion: String,
    val clientName: String,
    val platformIdentity: PlatformIdentity,
    val scopes: List<String>,
    val requiredRoleOid: String,
    val tokenLifetimeSeconds: Long = 300L,
    val aslProdEnvironment: Boolean = true,
    val attestation: AttestationStrategy = AttestationStrategy.Software,
    val tokenProvider: TokenProviderConfig,
    /**
     * DEV/TEST ONLY: PEM-encoded CA certificate the eGK WebSocket transport trusts instead of the
     * platform trust store, so it can reach the self-signed local docker ingress without disabling
     * TLS validation. Must be null in production (platform trust store applies).
     */
    val trustedCaPem: String? = null,
) {
    init {
        require(fqdn.isNotBlank()) { "fqdn must not be blank" }
        require(scopes.isNotEmpty()) { "scopes must not be empty" }
        require(tokenLifetimeSeconds > 0L) { "tokenLifetimeSeconds must be > 0" }
        require(requiredRoleOid.isNotBlank()) { "requiredRoleOid must not be blank" }
    }
}

/**
 * Callback interface the host app implements to supply a signed subject token
 * to the ZETA authentication layer.
 *
 * For [TokenProviderConfig.Egk]: implementation reads the insured person's eGK
 * via NFC and returns a signed JWT.
 * For [TokenProviderConfig.GesundheitsId]: implementation drives a GesundheitsId
 * OIDC flow and returns the resulting ID token.
 */
fun interface PoppSubjectTokenProvider {
    suspend fun createSubjectToken(): String
}

/**
 * Selects which subject-token strategy the ZETA engine uses for authentication.
 */
sealed interface TokenProviderConfig {
    /** Insured person authenticates via eGK (electronic health card, NFC). */
    data class Egk(val provider: PoppSubjectTokenProvider) : TokenProviderConfig

    /** Insured person authenticates via GesundheitsId (OIDC digital identity). */
    data class GesundheitsId(val provider: PoppSubjectTokenProvider) : TokenProviderConfig
}

/** SDK-internal device-only token provider — not exposed to host apps. */
internal data object DeviceOnly : TokenProviderConfig

/**
 * Selects how the device identifies itself in the ZETA `platform_product_id` claim.
 */
sealed interface PlatformIdentity {
    data class Android(
        val packageName: String,
        val sha256CertFingerprints: List<String>,
    ) : PlatformIdentity

    data class Apple(
        val platformType: String,
        val appBundleIds: List<String>,
    ) : PlatformIdentity
}

/**
 * Attestation strategy passed to the ZETA AuthConfig.
 *
 * [Software] uses a software key (only acceptable for development); production
 * deployments should use [TpmHttp] to bind attestation to the platform TEE.
 */
sealed interface AttestationStrategy {
    data object Software : AttestationStrategy

    data class TpmHttp(
        val attestationEndpoint: String,
        val pcrSelection: List<Int> = listOf(23),
        val websocketEndpoint: String? = null,
    ) : AttestationStrategy
}