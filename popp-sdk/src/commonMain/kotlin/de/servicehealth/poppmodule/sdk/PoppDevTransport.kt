package de.servicehealth.poppmodule.sdk

/**
 * Opt-in marker for the DEV/TEST-only direct (non-ZETA) PoPP transport. Production hosts must
 * use [PoppSdk.start]; [PoppSdk.directTransport] bypasses the ZETA Guard handshake and is only
 * for local-stack development (POPPM-161). Greppable: search `PoppDevTransport` for every use.
 */
@RequiresOptIn(
    message = "Direct (non-ZETA) PoPP transport is DEV/TEST only — production must use PoppSdk.start().",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
annotation class PoppDevTransport
