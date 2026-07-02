package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_title
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

/** Placeholder for POPPM-160 (Error). Thin: AC title/text + the failure category. */
@Composable
fun ErrorPlaceholderScreen(
    failure: String?,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(c.deep).safeContentPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.checkin_error_title), color = c.white, style = BrandTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.checkin_error_subtitle), color = c.white.copy(alpha = 0.8f), style = BrandTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (failure != null) {
            Spacer(Modifier.height(8.dp))
            Text(failure, color = c.white.copy(alpha = 0.6f), style = BrandTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
        BrandButton(text = "OK", onClick = onClose)
    }
}
