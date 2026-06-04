package de.servicehealth.poppmodule.demo.ui.apptoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme

/** Placeholder for the App-zu-App home. Replaced by the real screen in its own ticket. */
@Composable
fun AppToAppHomeScreen(scenarioId: String?) {
    val sdk = LocalPoppSdk.current
    val c = BrandTheme.colors
    Box(
        modifier = Modifier.fillMaxSize().background(c.mist).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "App-to-App — ${scenarioId ?: "?"} (placeholder)",
                color = c.ink,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = sdk.platformInfo(),
                color = c.neutral700,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
