package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.servicehealth.poppmodule.sdk.qr.ScanResult

@Composable
expect fun QrCameraViewfinder(
    onResult: (ScanResult) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
)
