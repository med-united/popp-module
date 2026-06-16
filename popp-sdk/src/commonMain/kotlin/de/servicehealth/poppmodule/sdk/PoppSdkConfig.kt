package de.servicehealth.poppmodule.sdk

/**
 * Configuration passed to [PoppSdk.start]. Carries everything the underlying
 * ZETA client needs to register and attest at the ZETA Guard, plus the
 * deployment-specific values (scopes, role OID, token provider) that the host
 * app supplies.
 *
 * Field mapping to `de.gematik.zeta.sdk.BuildConfig` / `AuthConfig` is done
 * internally by the platform engine.
 */
data class PoppSdkConfig(
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
 * Selects how the device identifies itself in the ZETA `platform_product_id`
 * claim. Use [Android] in androidMain hosts and [Apple] in iOS hosts.
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

/**
 * Source of the bearer token that the ZETA client uses to authenticate. Only
 * [Smb] (SMB-C keystore credentials) is modelled for now.
 */
sealed interface TokenProviderConfig {
    data class Smb(
        val alias: String,
        val password: String,
        val keystoreFile: String = "",
        val keystoreB64: String = "",
    ) : TokenProviderConfig {
        init {
            require(keystoreFile.isNotEmpty() || keystoreB64.isNotEmpty()) {
                "either keystoreFile or keystoreB64 must be provided"
            }
        }
    }
}
