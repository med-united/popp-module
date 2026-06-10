package de.servicehealth.poppmodule.sdk.egk.nfc

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.BCProvider
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.Bytes
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.CardUtilities
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.PaceKey
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CommandApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.ResponseApdu
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.KeyDerivationFunction
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.KeyDerivationFunction.getAES128Key
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.paceAuthTokenMac
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.paceSharedSecret
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.tagobjects.DataObject
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.tagobjects.MacObject
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.tagobjects.StatusObject
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * In-process eGK behind [ICardChannel]: answers the card (PICC) side of PACE with the
 * given CAN, then unwraps secure-messaging commands and wraps responses with the
 * negotiated key. Logical (unwrapped) APDUs are answered by [logicalResponder] —
 * default echoes the plain command back as response data with SW 9000.
 */
@Suppress("MagicNumber")
internal class FakePaceCard(
    private val can: String,
    var logicalResponder: (ByteArray) -> ByteArray = { plain -> plain + STATUS_OK },
) : ICardChannel {

    override var maxTransceiveLength = 65535
    override var isExtendedLengthSupported = true

    /** Test hooks. */
    var throwOnNextTransmit: IOException? = null
    var corruptNextResponseMac = false

    private val curve = ECNamedCurveTable.getParameterSpec("BrainpoolP256r1")
    private val random = SecureRandom()

    private var gaStep = 0
    private lateinit var nonceS: ByteArray
    private lateinit var mappedGenerator: ECPoint
    private lateinit var pcdPk2: ByteArray
    private lateinit var cardPk2: ByteArray
    private var negotiatedKey: PaceKey? = null
    private var paceKey: PaceKey? = null // non-null once mutual auth succeeded → SM active
    private val ssc = ByteArray(16)

    override fun transmit(command: CommandApdu): ResponseApdu {
        throwOnNextTransmit?.let { throwOnNextTransmit = null; throw it }
        val apdu = command.bytes
        return ResponseApdu(if (paceKey != null) handleSecureMessaging(apdu) else handlePlain(apdu))
    }

    // ---- plain phase: SELECT / READ / MSE / GENERAL AUTHENTICATE -------------------------

    private fun handlePlain(apdu: ByteArray): ByteArray =
        when (apdu[1].toInt() and 0xFF) {
            0xA4 -> STATUS_OK // SELECT (root and EF.CardAccess)
            0xB0 -> EF_CARD_ACCESS + STATUS_OK // READ BINARY of the selected EF.CardAccess
            0x22 -> STATUS_OK // MSE:SET AT (PACE with CAN)
            0x86 -> handleGeneralAuthenticate(apdu)
            else -> error("FakePaceCard: unexpected plain APDU ${Hex.toHexString(apdu)}")
        }

    private fun handleGeneralAuthenticate(apdu: ByteArray): ByteArray {
        gaStep++
        return when (gaStep) {
            1 -> { // step 1: provide encrypted nonce z = E(K_can, s)
                nonceS = ByteArray(16).also(random::nextBytes)
                val kCan = getAES128Key(can.toByteArray(), KeyDerivationFunction.Mode.PASSWORD)
                val z = ByteArray(16)
                AESEngine.newInstance().apply { init(true, KeyParameter(kCan)) }
                    .processBlock(nonceS, 0, z, 0)
                gaResponse(0x80, z)
            }
            2 -> { // step 2: generic mapping — receive PCD PK1, return card PK1
                val pcdPk1 = extractGaPayload(apdu)
                val sk1 = randomScalar()
                val cardPk1 = curve.g.multiply(sk1)
                val shared = CardUtilities.byteArrayToECPoint(pcdPk1, curve.curve).multiply(sk1)
                mappedGenerator = curve.g.multiply(BigInteger(1, nonceS)).add(shared)
                gaResponse(0x82, cardPk1.getEncoded(false))
            }
            3 -> { // step 3: ephemeral keys on mapped generator — derive shared secret K
                pcdPk2 = extractGaPayload(apdu)
                val sk2 = randomScalar()
                cardPk2 = mappedGenerator.multiply(sk2).getEncoded(false)
                val k = CardUtilities.byteArrayToECPoint(pcdPk2, curve.curve).multiply(sk2)
                // Fixed-length FE2OS like a conformant card, matching the PCD side.
                val kBytes = paceSharedSecret(k)
                negotiatedKey = PaceKey(
                    getAES128Key(kBytes, KeyDerivationFunction.Mode.ENC),
                    getAES128Key(kBytes, KeyDerivationFunction.Mode.MAC),
                )
                gaResponse(0x84, cardPk2)
            }
            4 -> { // step 4: verify PCD token; on success return card token, SM active
                val pcdMac = extractGaPayload(apdu)
                val key = checkNotNull(negotiatedKey)
                val expected = paceAuthTokenMac(key.mac, cardPk2, PACE_PROTOCOL_ID)
                if (!pcdMac.contentEquals(expected)) {
                    gaStep = 0
                    negotiatedKey = null
                    byteArrayOf(0x63.toByte(), 0x00) // authentication failed
                } else {
                    paceKey = key
                    gaResponse(0x86, paceAuthTokenMac(key.mac, pcdPk2, PACE_PROTOCOL_ID))
                }
            }
            else -> error("FakePaceCard: unexpected GA step $gaStep")
        }
    }

    private fun randomScalar() = BigInteger(1, ByteArray(32).also(random::nextBytes))

    /** GA command data is the explicit APPLICATION-28 structure; reuse the PCD-side extractor. */
    private fun extractGaPayload(apdu: ByteArray): ByteArray {
        val lc = apdu[4].toInt() and 0xFF
        return CardUtilities.extractKeyObjectEncoded(apdu.copyOfRange(5, 5 + lc))
    }

    /**
     * Card GA response: explicit APPLICATION-28 wrapping a primitive context-specific DO whose
     * wire tag byte is [wireTag] (0x80/0x82/0x84/0x86). BC encodes [DERTaggedObject]'s tag number
     * (not the wire byte), so the context tag number is `wireTag and 0x1F` — this yields the exact
     * single-byte tag the PCD's `extractKeyObjectEncoded` strips. See deviation note in the task report.
     */
    private fun gaResponse(wireTag: Int, payload: ByteArray): ByteArray =
        DERTaggedObject(
            true,
            BERTags.APPLICATION,
            28,
            DERTaggedObject(false, wireTag and 0x1F, DEROctetString(payload)),
        ).encoded + STATUS_OK

    // ---- secure-messaging phase ----------------------------------------------------------

    private fun handleSecureMessaging(apdu: ByteArray): ByteArray {
        val key = checkNotNull(paceKey)
        incrementSsc() // command direction
        val plainCommand = unwrapCommand(apdu, key)
        val plainResponse = logicalResponder(plainCommand)
        incrementSsc() // response direction
        return wrapResponse(plainResponse, key)
    }

    /** Decrypts DO87 if present and restores the Le from DO97; reconstructs the short-form
     *  plain command `header [+ lc + data] [+ le]` (command MAC not re-verified —
     *  the PCD-side wrapping is pinned by the ported SecureMessagingTest vectors). */
    private fun unwrapCommand(apdu: ByteArray, key: PaceKey): ByteArray {
        val cmd = parseCommandApdu(apdu)
        val body = cmd.bytes.copyOfRange(cmd.dataOffset, cmd.dataOffset + cmd.rawNc)
        var data: ByteArray? = null
        var le: ByteArray? = null
        var offset = 0
        while (offset < body.size) {
            val tag = body[offset].toInt() and 0xFF
            val (len, lenSize) = readDerLength(body, offset + 1)
            val content = body.copyOfRange(offset + 1 + lenSize, offset + 1 + lenSize + len)
            when (tag) {
                0x87 -> data = Bytes.unPadData( // content[0] is the 0x01 padding indicator
                    aesCbcWithSscIv(Cipher.DECRYPT_MODE, key.enc, content.copyOfRange(1, content.size)),
                )
                0x97 -> le = content
                0x8E -> {} // command MAC, not re-verified (see KDoc)
                else -> error("FakePaceCard: unexpected SM data object 0x${tag.toString(16)}")
            }
            offset += 1 + lenSize + len
        }
        val headerPlain = byteArrayOf(
            (apdu[0].toInt() and 0x0C.inv()).toByte(), apdu[1], apdu[2], apdu[3],
        )
        val lcAndData = data?.let {
            require(it.size <= 255) { "FakePaceCard reconstructs short-form commands only (${it.size} data bytes)" }
            byteArrayOf(it.size.toByte()) + it
        } ?: ByteArray(0)
        le?.let { require(it.size == 1) { "FakePaceCard reconstructs short-form Le only (DO97 of ${it.size} bytes)" } }
        return headerPlain + lcAndData + (le ?: ByteArray(0))
    }

    private fun wrapResponse(plainResponse: ByteArray, key: PaceKey): ByteArray {
        val sw = plainResponse.copyOfRange(plainResponse.size - 2, plainResponse.size)
        val data = plainResponse.copyOfRange(0, plainResponse.size - 2)
        val out = ByteArrayOutputStream()
        if (data.isNotEmpty()) {
            val encrypted = byteArrayOf(0x01) +
                aesCbcWithSscIv(Cipher.ENCRYPT_MODE, key.enc, Bytes.padData(data, 16))
            DataObject(encrypted).taggedObject.encodeTo(out)
        }
        StatusObject(sw).taggedObject.encodeTo(out)
        var mac = MacObject(commandOutput = out, kMac = key.mac, ssc = ssc).mac
        if (corruptNextResponseMac) {
            corruptNextResponseMac = false
            mac = mac.clone().also { it[0] = (it[0].toInt() xor 0xFF).toByte() }
        }
        out.write(byteArrayOf(0x8E.toByte(), 0x08))
        out.write(mac)
        out.write(sw)
        return out.toByteArray()
    }

    private fun incrementSsc() {
        for (i in ssc.indices.reversed()) {
            ssc[i]++
            if (ssc[i] != 0.toByte()) break
        }
    }

    private fun aesCbcWithSscIv(mode: Int, key: ByteArray, data: ByteArray): ByteArray {
        val iv = Cipher.getInstance("AES/ECB/NoPadding", BCProvider).run {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
            doFinal(ssc)
        }
        return Cipher.getInstance("AES/CBC/NoPadding", BCProvider).run {
            init(mode, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            doFinal(data)
        }
    }

    private fun readDerLength(bytes: ByteArray, offset: Int): Pair<Int, Int> {
        val first = bytes[offset].toInt() and 0xFF
        return when {
            first < 0x80 -> first to 1
            first == 0x81 -> (bytes[offset + 1].toInt() and 0xFF) to 2
            first == 0x82 ->
                (((bytes[offset + 1].toInt() and 0xFF) shl 8) or (bytes[offset + 2].toInt() and 0xFF)) to 3
            else -> error("unsupported DER length form 0x${first.toString(16)}")
        }
    }

    companion object {
        val STATUS_OK = byteArrayOf(0x90.toByte(), 0x00)

        /** id-PACE-ECDH-GM-AES-CBC-CMAC-128 — matches [EF_CARD_ACCESS]. */
        const val PACE_PROTOCOL_ID = "0.4.0.127.0.7.2.2.4.2.2"

        /** EF.CardAccess: PACE-ECDH-GM-AES-CBC-CMAC-128, brainpoolP256r1 (parameter id 13). */
        val EF_CARD_ACCESS: ByteArray =
            Hex.decode("31143012060A04007F0007020204020202010202010D")
    }
}
