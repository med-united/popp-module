package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.runtime.Composable

/** iOS has no NFC eGK channel yet (the SDK's EgkNfcChannel is Android-only) — report unsupported. */
@Composable
actual fun rememberEgkChannelSource(): EgkChannelSource = UnsupportedEgkChannelSource
