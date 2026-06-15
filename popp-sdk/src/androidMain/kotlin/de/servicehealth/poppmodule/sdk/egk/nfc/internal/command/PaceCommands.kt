package de.servicehealth.poppmodule.sdk.egk.nfc.internal.command

import de.servicehealth.poppmodule.sdk.egk.nfc.internal.card.ICardChannel
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * The five plain (pre-secure-messaging) commands the PACE handshake sends
 * (gemSpec_COS #14.2.6, #14.3.2, #14.9.9.7, #14.7.2). The handshake is the only place
 * this module builds plain commands itself — everything else arrives ready-made from
 * the PoPP-Service — so these are constructed directly as [CommandApdu]s instead of
 * porting E-Rezept's HealthCardCommand DSL and status-word tables.
 * PaceCommandsTest pins each builder to the exact bytes the upstream DSL produces.
 */
@Suppress("MagicNumber")
internal object PaceCommands {
    /** SELECT MF, request FCP (gemSpec_COS#14.2.6.2). */
    fun selectRoot(extendedLength: Boolean): CommandApdu =
        CommandApdu.ofOptions(0x00, 0xA4, 0x04, 0x04, null, expectAll(extendedLength))

    /** SELECT EF.CardAccess by FID 011C, no FCP (gemSpec_COS#14.2.6.13). */
    fun selectEfCardAccess(): CommandApdu =
        CommandApdu.ofOptions(0x00, 0xA4, 0x02, 0x0C, byteArrayOf(0x01, 0x1C), null)

    /** READ BINARY of the selected EF from offset 0 (gemSpec_COS#14.3.2.1). */
    fun readBinary(extendedLength: Boolean): CommandApdu =
        CommandApdu.ofOptions(0x00, 0xB0, 0x00, 0x00, null, expectAll(extendedLength))

    /** MSE:SET AT — select PACE with the CAN, symmetric key reference 2 (gemSpec_COS#14.9.9.7). */
    fun mseSetAtPaceCan(paceInfoProtocolBytes: ByteArray): CommandApdu =
        CommandApdu.ofOptions(
            0x00,
            0x22,
            0xC1,
            0xA4,
            // '80 I2OS(OctetLength(OID), 1) || OID || 83 01 02'
            DERTaggedObject(false, 0, DEROctetString(paceInfoProtocolBytes)).encoded +
                DERTaggedObject(false, 3, DEROctetString(byteArrayOf(2))).encoded,
            null,
        )

    /** GENERAL AUTHENTICATE step 1 — request nonce (gemSpec_COS#14.7.2.1.1). */
    fun generalAuthenticate(commandChaining: Boolean): CommandApdu =
        CommandApdu.ofOptions(
            cla(commandChaining),
            0x86,
            0x00,
            0x00,
            DERTaggedObject(false, BERTags.APPLICATION, 28, DERSequence()).encoded,
            EXPECTED_LENGTH_WILDCARD_SHORT,
        )

    /** GENERAL AUTHENTICATE steps 2a/3a/5a with payload under context tag 1/3/5 (gemSpec_COS#14.7.2.1.1). */
    fun generalAuthenticate(
        commandChaining: Boolean,
        data: ByteArray,
        tagNo: Int,
    ): CommandApdu =
        CommandApdu.ofOptions(
            cla(commandChaining),
            0x86,
            0x00,
            0x00,
            DERTaggedObject(
                true,
                BERTags.APPLICATION,
                28,
                DERTaggedObject(false, tagNo, DEROctetString(data)),
            ).encoded,
            EXPECTED_LENGTH_WILDCARD_SHORT,
        )

    private fun cla(commandChaining: Boolean): Int = if (commandChaining) 0x10 else 0x00

    private fun expectAll(extendedLength: Boolean): Int =
        if (extendedLength) EXPECTED_LENGTH_WILDCARD_EXTENDED else EXPECTED_LENGTH_WILDCARD_SHORT
}

/** A plain handshake command returned SW != 9000 (replaces E-Rezept's ResponseStatus/ResponseException). */
internal class CardResponseException(val sw: Int, step: String) :
    Exception("$step failed with SW=0x${sw.toString(16).uppercase()}")

/** Transmits [command] and requires SW 9000 (replaces E-Rezept's executeSuccessfulOn). */
@Suppress("MagicNumber")
internal fun ICardChannel.transmitSuccessfully(
    step: String,
    command: CommandApdu,
): ResponseApdu =
    transmit(command).also { if (it.sw != 0x9000) throw CardResponseException(it.sw, step) }
