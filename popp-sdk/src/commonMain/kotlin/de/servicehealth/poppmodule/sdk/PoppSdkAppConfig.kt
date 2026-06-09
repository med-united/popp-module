package de.servicehealth.poppmodule.sdk

/**
 * Host-app-provided configuration for the PoPP SDK.
 *
 * Pass an instance to [PoppSdk] at construction time. The fqdn of the PoPP service
 * is provided separately via [PoppSdk.init].
 *
 * Spec-level constants (productId, scopes, requiredRoleOid) are managed internally
 * by the SDK and are not exposed here.
 */
data class PoppSdkAppConfig(
    val clientName: String,
    val platformIdentity: PlatformIdentity,
    val tokenProvider: TokenProviderConfig,
    val attestation: AttestationStrategy = AttestationStrategy.Software,
    val tokenLifetimeSeconds: Long = 300L,
    val aslProdEnvironment: Boolean = true,
) {
    init {
        require(clientName.isNotBlank()) { "clientName must not be blank" }
        require(tokenLifetimeSeconds > 0L) { "tokenLifetimeSeconds must be > 0" }
    }
}