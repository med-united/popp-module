package de.servicehealth.poppmodule.sdk.internal

import de.gematik.zeta.sdk.TpmConfig
import de.gematik.zeta.sdk.ZetaSdk
import de.gematik.zeta.sdk.ZetaSdkClient
import de.gematik.zeta.sdk.attestation.model.PlatformProductId
import de.gematik.zeta.sdk.authentication.AuthConfig
import de.gematik.zeta.sdk.authentication.SubjectTokenProvider
import de.gematik.zeta.sdk.network.http.client.ZetaHttpClientBuilder
import de.gematik.zeta.sdk.storage.StorageConfig
import de.gematik.zeta.sdk.tpm.TpmProvider
import de.servicehealth.poppmodule.sdk.AttestationStrategy
import de.servicehealth.poppmodule.sdk.DeviceOnly
import de.servicehealth.poppmodule.sdk.PlatformIdentity
import de.servicehealth.poppmodule.sdk.PoppSdkConfig
import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.PoppSubjectTokenProvider
import de.servicehealth.poppmodule.sdk.TokenProviderConfig
import de.servicehealth.poppmodule.sdk.storage.SecureStorage
import de.gematik.zeta.sdk.BuildConfig as ZetaBuildConfig
import de.gematik.zeta.sdk.attestation.model.AttestationConfig as ZetaAttestationConfig

internal class AndroidZetaEngine(
    private val config: PoppSdkConfig,
    storage: SecureStorage,
) : ZetaEngine {
    private val zetaClient: ZetaSdkClient =
        ZetaSdk.build(
            resource = config.fqdn,
            config = config.toZetaBuildConfig(SdkStorageAdapter(storage)),
        )

    override suspend fun start() {
        zetaClient.discover().getOrElse { throw it.toPoppSdkError("ZETA discover() failed") }
        zetaClient.register().getOrElse { throw it.toPoppSdkError("ZETA register() failed") }
        zetaClient.authenticate().getOrElse { throw it.toPoppSdkError("ZETA authenticate() failed") }
    }

    override suspend fun status(): String =
        zetaClient.status().fold(
            onSuccess = { it.name },
            onFailure = { throw it.toPoppSdkError("ZETA status() failed") },
        )

    override suspend fun hello(): String {
        val httpClient = zetaClient.httpClient {}
        return httpClient.get("/hellozeta").bodyAsText()
    }
}

internal actual fun createZetaEngine(
    config: PoppSdkConfig,
    storage: SecureStorage,
): ZetaEngine = AndroidZetaEngine(config, storage)

private fun PoppSdkConfig.toZetaBuildConfig(storage: SdkStorageAdapter): ZetaBuildConfig {
    // ZetaHttpClient.android.kt only reads additionalCaPem (PEM strings), not additionalCaFile.
    // disableServerValidation is also only honoured by the JVM target, not Android.
    // So for self-signed test CAs we read the file here and call addCaPem() directly.
    val httpClientBuilder =
        System.getProperty("popp.integration.ca.pem.file")
            ?.let { path -> ZetaHttpClientBuilder().addCaPem(java.io.File(path).readText()) }
    return ZetaBuildConfig(
        productId = productId,
        productVersion = productVersion,
        clientName = clientName,
        storageConfig = StorageConfig.Custom(provider = storage),
        tpmConfig = object : TpmConfig {},
        authConfig =
            AuthConfig(
                scopes = scopes,
                exp = tokenLifetimeSeconds,
                aslProdEnvironment = aslProdEnvironment,
                subjectTokenProvider = tokenProvider.toZetaSubjectTokenProvider(),
                attestation = attestation.toZetaAttestationConfig(),
                requiredRoleOid = requiredRoleOid,
            ),
        platformProductId = platformIdentity.toZetaPlatformProductId(),
        httpClientBuilder = httpClientBuilder,
    )
}

private fun PlatformIdentity.toZetaPlatformProductId(): PlatformProductId =
    when (this) {
        is PlatformIdentity.Android ->
            PlatformProductId.AndroidProductId(
                packageName = packageName,
                sha256CertFingerprints = sha256CertFingerprints,
            )

        is PlatformIdentity.Apple ->
            PlatformProductId.AppleProductId(
                platformType = platformType,
                appBundleIds = appBundleIds,
            )
    }

private fun AttestationStrategy.toZetaAttestationConfig(): ZetaAttestationConfig =
    when (this) {
        AttestationStrategy.Software -> ZetaAttestationConfig.Software
        is AttestationStrategy.TpmHttp ->
            ZetaAttestationConfig.TpmHttp(
                attestationEndpoint = attestationEndpoint,
                pcrSelection = pcrSelection,
                websocketEndpoint = websocketEndpoint,
            )
    }

private fun TokenProviderConfig.toZetaSubjectTokenProvider(): SubjectTokenProvider =
    when (this) {
        is TokenProviderConfig.Egk -> PoppSubjectTokenProviderAdapter(provider)
        is TokenProviderConfig.GesundheitsId -> PoppSubjectTokenProviderAdapter(provider)
        is DeviceOnly -> DeviceOnlyTokenProvider()
    }

private class PoppSubjectTokenProviderAdapter(
    private val poppProvider: PoppSubjectTokenProvider,
) : SubjectTokenProvider {
    override suspend fun createSubjectToken(
        clientId: String,
        dpopKey: String,
        nonceBytes: ByteArray,
        audience: String,
        now: Long,
        expiration: Long,
        tpmProvider: TpmProvider,
    ): String = poppProvider.createSubjectToken()
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
