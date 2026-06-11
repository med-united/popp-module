package de.servicehealth.poppmodule.sdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PoppSdkConfigTest {

    private fun validConfig(
        fqdn: String = "https://popp.example.test",
        scopes: List<String> = listOf("openid"),
        requiredRoleOid: String = "1.2.276.0.76.4.156",
        tokenLifetimeSeconds: Long = 300,
    ): PoppSdkConfig = PoppSdkConfig(
        fqdn = fqdn,
        productId = "de.servicehealth.popp",
        productVersion = "0.0.1",
        clientName = "popp-sdk-test",
        platformIdentity = PlatformIdentity.Android(
            packageName = "de.servicehealth.poppmodule",
            sha256CertFingerprints = listOf("AA:BB:CC"),
        ),
        scopes = scopes,
        requiredRoleOid = requiredRoleOid,
        tokenLifetimeSeconds = tokenLifetimeSeconds,
        tokenProvider = TokenProviderConfig.Egk(
            provider = { "stub-token" }
        ),
    )

    @Test
    fun valid_config_constructs() {
        val cfg = validConfig()
        assertEquals("popp-sdk-test", cfg.clientName)
        assertEquals(AttestationStrategy.Software, cfg.attestation)
    }

    @Test
    fun blank_fqdn_is_rejected() {
        assertFailsWith<IllegalArgumentException> { validConfig(fqdn = "") }
    }

    @Test
    fun empty_scopes_are_rejected() {
        assertFailsWith<IllegalArgumentException> { validConfig(scopes = emptyList()) }
    }

    @Test
    fun zero_token_lifetime_is_rejected() {
        assertFailsWith<IllegalArgumentException> { validConfig(tokenLifetimeSeconds = 0) }
    }

    @Test
    fun blank_role_oid_is_rejected() {
        assertFailsWith<IllegalArgumentException> { validConfig(requiredRoleOid = "") }
    }

    @Test
    fun version_isNotBlank() {
        assertTrue(PoppSdk().version().isNotBlank())
    }

    @Test
    fun platformInfo_reportsPlatformName() {
        assertTrue(PoppSdk().platformInfo().contains(getPlatform().name))
    }

    @Test
    fun init_accepts_valid_fqdn() {
        val poppSdk = PoppSdk()
        poppSdk.init("wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc")
    }

    @Test
    fun init_rejects_blank_fqdn() {
        val poppSdk = PoppSdk()
        assertFailsWith<IllegalArgumentException> { poppSdk.init("") }
    }

    @Test
    fun init_rejects_whitespace_only_fqdn() {
        val poppSdk = PoppSdk()
        assertFailsWith<IllegalArgumentException> { poppSdk.init("   ") }
    }
}

class PoppSdkErrorTest {

    @Test
    fun error_messages_round_trip() {
        val cause = RuntimeException("boom")
        val errs = listOf(
            PoppSdkError.Network("net", cause),
            PoppSdkError.Attestation("att", cause),
            PoppSdkError.Configuration("cfg", cause),
            PoppSdkError.PlatformUnsupported("pf"),
            PoppSdkError.Unknown("u", cause),
        )
        assertEquals(listOf("net", "att", "cfg", "pf", "u"), errs.map { it.message })
    }
}

class PoppSdkEnsureEngineTest {

    @Test
    fun status_before_init_throws_Configuration() = runTest {
        assertFailsWith<PoppSdkError.Configuration> { PoppSdk().status() }
    }

    @Test
    fun hello_before_init_throws_Configuration() = runTest {
        assertFailsWith<PoppSdkError.Configuration> { PoppSdk().hello() }
    }

    @Test
    fun status_with_fqdn_but_no_context_reports_missing_context() = runTest {
        val sdk = PoppSdk()
        sdk.init("wss://popp.example.test")
        val error = assertFailsWith<PoppSdkError.Configuration> { sdk.status() }
        assertTrue(
            error.message!!.contains("PoppSdk(context)"),
            "Expected context error but got: ${error.message}",
        )
    }
}

class TokenProviderConfigTest {

    @Test
    fun egk_provider_constructs() {
        val provider = TokenProviderConfig.Egk(
            provider = { "stub" }
        )
        assertTrue(provider is TokenProviderConfig.Egk)
    }

    @Test
    fun gesundheitsId_provider_constructs() {
        val provider = TokenProviderConfig.GesundheitsId(
            provider = { "stub" }
        )
        assertTrue(provider is TokenProviderConfig.GesundheitsId)
    }
}