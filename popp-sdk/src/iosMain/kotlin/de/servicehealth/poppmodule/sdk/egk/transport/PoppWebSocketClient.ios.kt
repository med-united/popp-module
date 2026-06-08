package de.servicehealth.poppmodule.sdk.egk.transport

import de.servicehealth.poppmodule.sdk.PoppSdkError
import io.ktor.client.HttpClient

/**
 * iOS stub. A Ktor Darwin engine + an iOS NFC `EgkApduChannel` are a later subtask; the loop, DTOs
 * and state machine already compile for iOS. Consistent with the SDK's Android-real / iOS-stub
 * posture (see `UnsupportedZetaEngine`). `checkInWithEgk` never reaches here on iOS — `PoppSdk.start`
 * already throws `PlatformUnsupported` there — but the `actual` is required for the module to link.
 */
internal actual fun createPoppWebSocketClient(disableTlsValidation: Boolean): HttpClient =
    throw PoppSdkError.PlatformUnsupported(
        "WebSocketScenarioTransport has no iOS engine yet (Ktor Darwin engine deferred).",
    )
