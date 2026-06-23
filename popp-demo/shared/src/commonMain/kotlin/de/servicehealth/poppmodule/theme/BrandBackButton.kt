package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class BrandBackButtonVariant { OnLight, OnDark }

/**
 * The "Zurück" pill used across every check-in step. Same shape, icon, and
 * spacing everywhere — only the colors flip between [BrandBackButtonVariant.OnLight]
 * (white screens) and [BrandBackButtonVariant.OnDark] (the QR scanner's camera view).
 */
@Composable
fun BrandBackButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BrandBackButtonVariant = BrandBackButtonVariant.OnLight,
) {
    val c = BrandTheme.colors
    val background = if (variant == BrandBackButtonVariant.OnDark) c.white.copy(alpha = 0.16f) else c.violet100
    val content = if (variant == BrandBackButtonVariant.OnDark) c.white else c.violet

    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = label,
            tint = content,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = content,
            style = BrandTheme.typography.labelLarge,
        )
    }
}

@Preview
@Composable
private fun BrandBackButtonOnLightPreview() {
    BrandTheme {
        Box(Modifier.background(BrandTheme.colors.white).padding(12.dp)) {
            BrandBackButton(label = "Zurück", onClick = {})
        }
    }
}

@Preview
@Composable
private fun BrandBackButtonOnDarkPreview() {
    BrandTheme {
        Box(Modifier.background(BrandTheme.colors.deep).padding(12.dp)) {
            BrandBackButton(label = "Zurück", onClick = {}, variant = BrandBackButtonVariant.OnDark)
        }
    }
}
