package de.servicehealth.poppmodule.sdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
        tokenProvider = TokenProviderConfig.Smb(
            alias = "smb-alias",
            password = "secret",
            keystoreB64 = "AAAA",
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
    fun smb_token_provider_requires_keystore_source() {
        assertFailsWith<IllegalArgumentException> {
            TokenProviderConfig.Smb(alias = "a", password = "p")
        }
    }

    @Test
    fun version_isNotBlank() {
        assertTrue(PoppSdk().version().isNotBlank())
    }

    @Test
    fun platformInfo_reportsPlatformName() {
        assertTrue(PoppSdk().platformInfo().contains(getPlatform().name))
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
            PoppSdkError.Protocol("proto", cause),
            PoppSdkError.Unknown("u", cause),
        )
        assertEquals(listOf("net", "att", "cfg", "pf", "proto", "u"), errs.map { it.message })
    }
}