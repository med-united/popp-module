package de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.BigIntegers
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Pins the PACE shared-secret encoding to the fixed-length FE2OS form (TR-03110-3): the
 * x-coordinate must always be 32 bytes on brainpoolP256r1, including when its leading
 * byte is zero. Upstream's BigInteger round trip dropped that leading zero on ~1 in 256
 * handshakes, deriving keys a real card does not — see [paceSharedSecret].
 */
class PaceSharedSecretTest {

    private val curve = ECNamedCurveTable.getParameterSpec("BrainpoolP256r1")

    @Test
    fun `shared secret keeps leading zero bytes of the x-coordinate`() {
        // Walk k*G deterministically until a point's x-coordinate is < 2^248 (~1 in 256 points)
        // — exactly the case where a variable-length BigInteger encoding loses a byte.
        var point = curve.g
        repeat(4096) {
            val x = point.normalize().xCoord.toBigInteger()
            if (x.bitLength() <= 248) {
                val encoded = paceSharedSecret(point)
                assertEquals(32, encoded.size)
                assertEquals(0, encoded[0].toInt())
                assertContentEquals(BigIntegers.asUnsignedByteArray(32, x), encoded)
                return
            }
            point = point.add(curve.g)
        }
        fail("no point with x < 2^248 within 4096 additions — statistically impossible")
    }

    @Test
    fun `shared secret is 32 bytes for ordinary x-coordinates`() {
        assertEquals(32, paceSharedSecret(curve.g).size)
    }
}
