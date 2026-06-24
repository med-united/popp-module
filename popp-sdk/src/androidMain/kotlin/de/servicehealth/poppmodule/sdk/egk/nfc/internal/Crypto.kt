package de.servicehealth.poppmodule.sdk.egk.nfc.internal

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom

/** Single BC provider instance shared by the ported card stack (registered ad hoc, not installed globally). */
internal val BCProvider = BouncyCastleProvider()

/**
 * Default SecureRandom, deliberately not getInstanceStrong(): the strong instance maps
 * to a blocking entropy source on desktop JVMs and can stall the PACE handshake tests
 * on CI; the non-blocking default is cryptographically sufficient here.
 */
internal fun secureRandomInstance(): SecureRandom = SecureRandom()
