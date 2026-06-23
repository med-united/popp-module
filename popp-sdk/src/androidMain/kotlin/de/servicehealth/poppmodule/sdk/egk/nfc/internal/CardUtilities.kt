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

package de.servicehealth.poppmodule.sdk.egk.nfc.internal

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

/**
 * Utility class for card functions
 */
internal object CardUtilities {
    private const val UNCOMPRESSEDPOINTVALUE = 0x04

    /**
     * Decodes an ECPoint from byte array. Prime field p is taken from the passed curve
     * The first byte must contain the value 0x04 (uncompressed point).
     *
     * @param byteArray Byte array of the form {0x04 || x-bytes [] || y byte []}
     * @param curve     The curve on which the point should lie.
     * @return EC point generated from input data
     */
    fun byteArrayToECPoint(
        byteArray: ByteArray,
        curve: ECCurve,
    ): ECPoint {
        return if (byteArray[0] != UNCOMPRESSEDPOINTVALUE.toByte()) {
            throw IllegalArgumentException("Found no uncompressed point!")
        } else {
            val x = ByteArray((byteArray.size - 1) / 2)
            val y = ByteArray((byteArray.size - 1) / 2)

            System.arraycopy(byteArray, 1, x, 0, (byteArray.size - 1) / 2)
            System.arraycopy(
                byteArray,
                1 + (byteArray.size - 1) / 2,
                y,
                0,
                (byteArray.size - 1) / 2,
            )
            curve.createPoint(BigInteger(1, x), BigInteger(1, y))
        }
    }

    /**
     * Encodes an ASN1 KeyObject
     */
    fun extractKeyObjectEncoded(asn1Input: ByteArray): ByteArray =
        ASN1InputStream(asn1Input).use { asn1InputStream ->
            val seq = asn1InputStream.readObject() as ASN1TaggedObject
            val seqObj: ASN1Object = seq.baseObject
            stripDerTlvHeader(seqObj.encoded)
        }

    /**
     * Strips the DER TLV header (tag + length octets) from [encoded], returning the value bytes.
     * Handles both short form (length < 128, a single length octet) and long form (length >= 128,
     * a leading `0x8n` octet followed by `n` length octets). A fixed 2-byte strip corrupts values
     * of 128 bytes or more — e.g. a BrainpoolP512r1 EC point (a 129-byte octet string needs two
     * length octets).
     */
    fun stripDerTlvHeader(encoded: ByteArray): ByteArray {
        val lengthOctet = encoded[1].toInt() and 0xFF
        val headerSize =
            if (lengthOctet and 0x80 == 0) {
                2 // tag + single short-form length octet
            } else {
                2 + (lengthOctet and 0x7F) // tag + leading 0x8n octet + n long-form length octets
            }
        return encoded.copyOfRange(headerSize, encoded.size)
    }
}
