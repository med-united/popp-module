package de.servicehealth.poppmodule.demo.ui.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.launcher_subline
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.launcher_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.servicehealth_logo
import de.servicehealth.poppmodule.theme.BrandTag
import de.servicehealth.poppmodule.theme.BrandTagTone
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The deep navy brand header band: solid tag + white wordmark + title + subline. */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
) {
    val c = BrandTheme.colors
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(c.deep)
                .safeContentPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BrandTag(text = "PoPP-Modul", tone = BrandTagTone.Solid)
            Image(
                painter = painterResource(Res.drawable.servicehealth_logo),
                contentDescription = "service·health",
                colorFilter = ColorFilter.tint(c.white),
                modifier = Modifier.height(26.dp),
            )
            Text(
                text = stringResource(Res.string.launcher_title),
                color = c.white,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(Res.string.launcher_subline),
                color = c.white.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview @Composable
private fun BrandHeaderPreview() {
    PreviewBrandTheme { BrandHeader() }
}
