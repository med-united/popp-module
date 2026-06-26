package de.servicehealth.poppmodule.sdk.federation

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import de.servicehealth.poppmodule.sdk.PoppSdkError
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Exercises [verifyJwsSignature] using freshly generated P-256 key pairs.
 * Runs on the JVM/Android host, using the Android (Nimbus) actual implementation.
 */
class FederationJwsVerifierTest {
    private fun makeJwk(): ECKey = ECKeyGenerator(Curve.P_256).generate()

    private fun sign(
        payload: String,
        jwk: ECKey,
    ): String {
        val jwsObj = JWSObject(JWSHeader(JWSAlgorithm.ES256), Payload(payload))
        jwsObj.sign(ECDSASigner(jwk))
        return jwsObj.serialize()
    }

    private fun ECKey.toFederationJwks(kid: String? = null) =
        FederationJwks(
            keys =
                listOf(
                    FederationJwk(
                        kty = "EC",
                        crv = "P-256",
                        x = x.toString(),
                        y = y.toString(),
                        kid = kid,
                    ),
                ),
        )

    @Test
    fun valid_signature_verifies_correctly() {
        val jwk = makeJwk()
        val jws = sign("federation-test", jwk)
        verifyJwsSignature(jws, jwk.toFederationJwks()) // must not throw
    }

    @Test
    fun tampered_payload_fails_verification() {
        val jwk = makeJwk()
        val segs = sign("original", jwk).split(".")
        val tampered = "${segs[0]}.dGFtcGVyZWQ.${segs[2]}"
        assertFailsWith<PoppSdkError.Protocol> { verifyJwsSignature(tampered, jwk.toFederationJwks()) }
    }

    @Test
    fun wrong_key_fails_verification() {
        val signingJwk = makeJwk()
        val wrongJwk = makeJwk()
        val jws = sign("test", signingJwk)
        assertFailsWith<PoppSdkError.Protocol> { verifyJwsSignature(jws, wrongJwk.toFederationJwks()) }
    }

    @Test
    fun no_ec_key_in_jwks_throws_protocol_error() {
        val jws = sign("test", makeJwk())
        assertFailsWith<PoppSdkError.Protocol> {
            verifyJwsSignature(jws, FederationJwks(keys = emptyList()))
        }
    }

    @Test
    fun kid_in_jws_header_routes_to_correct_key() {
        val correctJwk = makeJwk()
        val wrongJwk = makeJwk()

        // Sign with correctJwk using kid="k1" in the header
        val header = JWSHeader.Builder(JWSAlgorithm.ES256).keyID("k1").build()
        val jwsObj = JWSObject(header, Payload("kid-test"))
        jwsObj.sign(ECDSASigner(correctJwk))
        val jws = jwsObj.serialize()

        // JWKS has two keys: wrong key (no kid), correct key (kid="k1")
        val jwks =
            FederationJwks(
                keys =
                    listOf(
                        FederationJwk(kty = "EC", crv = "P-256", x = wrongJwk.x.toString(), y = wrongJwk.y.toString()),
                        FederationJwk(kty = "EC", crv = "P-256", x = correctJwk.x.toString(), y = correctJwk.y.toString(), kid = "k1"),
                    ),
            )
        verifyJwsSignature(jws, jwks) // selects kid="k1" → must not throw
    }

    @Test
    fun kid_mismatch_in_jwks_throws_protocol_error() {
        val jwk = makeJwk()
        val header = JWSHeader.Builder(JWSAlgorithm.ES256).keyID("missing-kid").build()
        val jwsObj = JWSObject(header, Payload("test"))
        jwsObj.sign(ECDSASigner(jwk))
        val jws = jwsObj.serialize()

        assertFailsWith<PoppSdkError.Protocol> {
            verifyJwsSignature(jws, jwk.toFederationJwks(kid = "other-kid"))
        }
    }
}
