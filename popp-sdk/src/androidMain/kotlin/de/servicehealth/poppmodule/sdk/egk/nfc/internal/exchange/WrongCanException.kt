package de.servicehealth.poppmodule.sdk.egk.nfc.internal.exchange

/**
 * PACE mutual authentication failed — by far the most likely cause is a CAN that does not
 * belong to the presented card. Mapped to PoppSdkError.Card(WRONG_CAN) by the channel.
 */
internal class WrongCanException(cause: Throwable? = null) :
    Exception("PACE mutual authentication failed (wrong CAN?)", cause)
