package de.servicehealth.poppmodule.sdk.internal

import de.gematik.zeta.sdk.BuildConfig as ZetaBuildConfig
import de.gematik.zeta.sdk.TpmConfig
import de.gematik.zeta.sdk.ZetaSdk
import de.gematik.zeta.sdk.ZetaSdkClient
import de.gematik.zeta.sdk.attestation.model.AttestationConfig as ZetaAttestationConfig
import de.gematik.zeta.sdk.attestation.model.PlatformProductId
import de.gematik.zeta.sdk.authentication.AuthConfig
import de.gematik.zeta.sdk.authentication.smb.SmbTokenProvider
import de.gematik.zeta.sdk.storage.StorageConfig
import de.servicehealth.poppmodule.sdk.AttestationStrategy
import de.servicehealth.poppmodule.sdk.PlatformIdentity
import de.servicehealth.poppmodule.sdk.PoppSdkConfig
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.TokenProviderConfig
import de.servicehealth.poppmodule.sdk.storage.SecureStorage

internal class AndroidZetaEngine(
    private val config: PoppSdkConfig,
    storage: SecureStorage,
) : ZetaEngine {

    private val client: ZetaSdkClient = ZetaSdk.build(
        resource = config.fqdn,
        config = config.toZetaBuildConfig(SdkStorageAdapter(storage)),
    )

    override suspend fun start() {
        client.discover().getOrElse { throw it.toPoppSdkError("ZETA discover() failed") }
        client.register().getOrElse { throw it.toPoppSdkError("ZETA register() failed") }
        client.authenticate().getOrElse { throw it.toPoppSdkError("ZETA authenticate() failed") }
    }

    override suspend fun status(): String =
        client.status().fold(
            onSuccess = { it.name },
            onFailure = { throw it.toPoppSdkError("ZETA status() failed") },
        )
}

internal actual fun createZetaEngine(
    context: PoppSdkContext,
    config: PoppSdkConfig,
    storage: SecureStorage,
): ZetaEngine = AndroidZetaEngine(config, storage)

private fun PoppSdkConfig.toZetaBuildConfig(storage: SdkStorageAdapter): ZetaBuildConfig =
    ZetaBuildConfig(
        productId = productId,
        productVersion = productVersion,
        clientName = clientName,
        storageConfig = StorageConfig.Custom(provider = storage),
        tpmConfig = object : TpmConfig {},
        authConfig = AuthConfig(
            scopes = scopes,
            exp = tokenLifetimeSeconds,
            aslProdEnvironment = aslProdEnvironment,
            subjectTokenProvider = tokenProvider.toZetaSubjectTokenProvider(),
            attestation = attestation.toZetaAttestationConfig(),
            requiredRoleOid = requiredRoleOid,
        ),
        platformProductId = platformIdentity.toZetaPlatformProductId(),
    )

private fun PlatformIdentity.toZetaPlatformProductId(): PlatformProductId = when (this) {
    is PlatformIdentity.Android -> PlatformProductId.AndroidProductId(
        packageName = packageName,
        sha256CertFingerprints = sha256CertFingerprints,
    )

    is PlatformIdentity.Apple -> PlatformProductId.AppleProductId(
        platformType = platformType,
        appBundleIds = appBundleIds,
    )
}

private fun AttestationStrategy.toZetaAttestationConfig(): ZetaAttestationConfig = when (this) {
    AttestationStrategy.Software -> ZetaAttestationConfig.Software
    is AttestationStrategy.TpmHttp -> ZetaAttestationConfig.TpmHttp(
        attestationEndpoint = attestationEndpoint,
        pcrSelection = pcrSelection,
        websocketEndpoint = websocketEndpoint,
    )
}

private fun TokenProviderConfig.toZetaSubjectTokenProvider() = when (this) {
    is TokenProviderConfig.Smb -> SmbTokenProvider(
        SmbTokenProvider.Credentials(
            keystoreFile = keystoreFile,
            alias = alias,
            password = password,
            keystoreB64 = keystoreB64,
        ),
    )
}

private fun Throwable.toPoppSdkError(message: String): PoppSdkError {
    val text = "$message: ${this.message ?: this::class.simpleName}"
    // Best-effort classification: anything containing "attest"/"register" → Attestation,
    // network exceptions → Network, otherwise Unknown.
    val lower = text.lowercase()
    return when {
        lower.contains("attest") || lower.contains("register") ->
            PoppSdkError.Attestation(text, this)

        lower.contains("network") || lower.contains("connect") || lower.contains("timeout") ||
            lower.contains("unreachable") || lower.contains("ssl") || lower.contains("tls") ->
            PoppSdkError.Network(text, this)

        else -> PoppSdkError.Unknown(text, this)
    }
}