/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * Modified by the PoPP-Module project (POPPM-119) — see NOTICE.md at the repository root.
 */

@file:Suppress("MagicNumber")

package de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.Bytes
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.CardUtilities.byteArrayToECPoint
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.CardUtilities.extractKeyObjectEncoded
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.PaceKey
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.CardResponseException
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.PaceCommands
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.command.transmitSuccessfully
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.KeyDerivationFunction
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange.KeyDerivationFunction.getAES128Key
import de.servicehealth.poppmodule.sdk.egk.nfc.internal.secureRandomInstance
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.params.KeyParameter
import java.math.BigInteger

private const val AES_BLOCK_SIZE = 16
private const val BYTE_LENGTH = 8
private const val MAX = 64
private const val TAG_6 = 6
private const val TAG_49 = 0x49

/**
 * Opens a secure PACE Channel for secure messaging
 *
 * picc = card
 * pcd = smartphone
 *
 * Performs blocking APDU I/O via [ICardChannel.transmit]; callers must run it on an
 * IO dispatcher.
 */
internal suspend fun ICardChannel.establishTrustedChannel(cardAccessNumber: String): PaceKey {
    val randomGenerator = secureRandomInstance()

    suspend fun step0ReadSupportedPaceParameters(step1: suspend (paceInfo: PaceInfo) -> PaceKey): PaceKey {
        transmitSuccessfully("SELECT MF", PaceCommands.selectRoot(isExtendedLengthSupported))

        transmitSuccessfully("SELECT EF.CardAccess", PaceCommands.selectEfCardAccess())

        // No status check, as upstream: a warning SW (e.g. end-of-file) still carries the data.
        val paceInfo = PaceInfo(transmit(PaceCommands.readBinary(isExtendedLengthSupported)).data)

        transmitSuccessfully(
            "MSE:SET AT (PACE with CAN)",
            PaceCommands.mseSetAtPaceCan(paceInfo.paceInfoProtocolBytes),
        )

        return step1(paceInfo)
    }

    suspend fun step1EphemeralPublicKeyFirst(
        paceInfo: PaceInfo,
        step2: suspend (
            paceInfo: PaceInfo,
            nonceSInt: BigInteger,
            pcdSkX1: BigInteger,
            pcdPk1: ByteArray
        ) -> PaceKey
    ): PaceKey {
        val nonceZBytes = transmitSuccessfully(
            "GENERAL AUTHENTICATE step 1",
            PaceCommands.generalAuthenticate(true),
        ).data
        val nonceZBytesEncoded = extractKeyObjectEncoded(nonceZBytes)
        val canBytes = cardAccessNumber.toByteArray()

        val aes128Key = getAES128Key(canBytes, KeyDerivationFunction.Mode.PASSWORD)
        val encKey = KeyParameter(aes128Key)

        val nonceS = ByteArray(AES_BLOCK_SIZE)
        AESEngine.newInstance().apply {
            init(false, encKey)
            processBlock(nonceZBytesEncoded, 0, nonceS, 0)
        }
        val nonceSInt = BigInteger(1, nonceS)

        val pk1Pcd = ByteArray(paceInfo.ecCurve.fieldSize / BYTE_LENGTH)
        randomGenerator.nextBytes(pk1Pcd)

        val pcdSkX1 = BigInteger(1, pk1Pcd)
        val pcdPkSkX1 = paceInfo.ecPointG.multiply(pcdSkX1)

        return step2(paceInfo, nonceSInt, pcdSkX1, pcdPkSkX1.getEncoded(false))
    }

    suspend fun step2EphemeralPublicKeySecond(
        paceInfo: PaceInfo,
        nonceSInt: BigInteger,
        pcdSkX1: BigInteger,
        pcdPk1: ByteArray,
        step3: suspend (
            paceInfo: PaceInfo,
            pcdSkX2: BigInteger,
            pcdPkS2: ByteArray
        ) -> PaceKey
    ): PaceKey {
        val piccPk1Bytes = transmitSuccessfully(
            "GENERAL AUTHENTICATE step 2",
            PaceCommands.generalAuthenticate(true, pcdPk1, 1),
        ).data

        val piccPk1BytesEncoded = extractKeyObjectEncoded(piccPk1Bytes)
        val y1 = byteArrayToECPoint(piccPk1BytesEncoded, paceInfo.ecCurve)
        val x2 = ByteArray(paceInfo.ecCurve.fieldSize / BYTE_LENGTH)
        randomGenerator.nextBytes(x2)

        val sharedSecretP = y1.multiply(pcdSkX1)
        val pointGS = paceInfo.ecPointG.multiply(nonceSInt).add(sharedSecretP)

        val pcdSkX2 = BigInteger(1, x2)
        val pcdPkS2 = pointGS.multiply(pcdSkX2)

        return step3(paceInfo, pcdSkX2, pcdPkS2.getEncoded(false))
    }

    suspend fun step3MutualAuthentication(
        paceInfo: PaceInfo,
        pcdSkX2: BigInteger,
        pcdPkS2: ByteArray,
        step4: suspend (
            piccMacDerived: ByteArray,
            pcdMac: ByteArray
        ) -> Boolean
    ): PaceKey {
        val piccPk2Bytes = transmitSuccessfully(
            "GENERAL AUTHENTICATE step 3",
            PaceCommands.generalAuthenticate(true, pcdPkS2, 3),
        ).data

        val piccPk2 = extractKeyObjectEncoded(piccPk2Bytes)

        val piccPk2ECPoint = byteArrayToECPoint(piccPk2, paceInfo.ecCurve)
        val sharedSecretK = piccPk2ECPoint.multiply(pcdSkX2)

        val sharedSecretKBytes: ByteArray =
            Bytes.bigIntToByteArray(sharedSecretK.normalize().xCoord.toBigInteger())

        val paceKey = PaceKey(
            getAES128Key(sharedSecretKBytes, KeyDerivationFunction.Mode.ENC),
            getAES128Key(sharedSecretKBytes, KeyDerivationFunction.Mode.MAC)
        )

        val pcdMac = paceAuthTokenMac(paceKey.mac, piccPk2, paceInfo.protocolID)
        val piccMacDerived = paceAuthTokenMac(paceKey.mac, pcdPkS2, paceInfo.protocolID)

        if (!step4(piccMacDerived, pcdMac)) throw WrongCanException()

        return paceKey
    }

    fun step4VerifyPcdAndPiccMac(
        piccMacDerived: ByteArray,
        pcdMac: ByteArray
    ): Boolean {
        val piccMacBytes = try {
            transmitSuccessfully(
                "GENERAL AUTHENTICATE step 4",
                PaceCommands.generalAuthenticate(false, pcdMac, 5),
            ).data
        } catch (e: CardResponseException) {
            throw WrongCanException(e)
        }

        val piccMac = extractKeyObjectEncoded(piccMacBytes)

        return piccMac.contentEquals(piccMacDerived)
    }

    /**
     * Negotiate the PaceKey and return the object
     */
    return step0ReadSupportedPaceParameters { paceInfo ->
        step1EphemeralPublicKeyFirst(paceInfo) { _, nonceSInt, pcdSkX1, pcdPk1 ->
            step2EphemeralPublicKeySecond(paceInfo, nonceSInt, pcdSkX1, pcdPk1) { _, pcdSkX2, pcdPkS2 ->
                step3MutualAuthentication(paceInfo, pcdSkX2, pcdPkS2) { piccMacDerived, pcdMac ->
                    step4VerifyPcdAndPiccMac(piccMacDerived, pcdMac)
                }
            }
        }
    }
}

internal fun paceAuthToken(ecPoint: ByteArray, protocolID: String): ByteArray {
    val asn1EncodableVector = ASN1EncodableVector()
    asn1EncodableVector.add(ASN1ObjectIdentifier(protocolID))
    asn1EncodableVector.add(
        DERTaggedObject(
            false,
            TAG_6,
            DEROctetString(ecPoint)
        )
    )
    return DERTaggedObject(false, BERTags.APPLICATION, TAG_49, DERSequence(asn1EncodableVector)).encoded
}

internal fun paceAuthTokenMac(macKey: ByteArray, publicKey: ByteArray, protocolID: String): ByteArray =
    CMac(AESEngine(), MAX).apply {
        init(KeyParameter(macKey))

        val authToken = paceAuthToken(publicKey, protocolID)
        update(authToken, 0, authToken.size)
    }.let {
        ByteArray(it.macSize).apply {
            it.doFinal(this, 0)
        }
    }
