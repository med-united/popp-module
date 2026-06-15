package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.sdk.qr.ScanResult
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.qr_scanner_ios_placeholder

@Composable
actual fun QrCameraViewfinder(
    onResult: (ScanResult) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val c = BrandTheme.colors
    LaunchedEffect(Unit) { onActiveChange(false) }
    Box(modifier.background(c.deep), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.qr_scanner_ios_placeholder),
            color = c.white,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )
    }
}
