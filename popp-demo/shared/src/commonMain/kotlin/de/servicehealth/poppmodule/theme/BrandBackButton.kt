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

@Composable
fun BrandBackButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BrandBackButtonVariant = BrandBackButtonVariant.OnLight,
) {
    val c = BrandTheme.colors
    val content = if (variant == BrandBackButtonVariant.OnDark) c.white else c.violet

    val styledModifier =
        if (variant == BrandBackButtonVariant.OnDark) {
            modifier
                .clip(CircleShape)
                .background(c.white.copy(alpha = 0.16f))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        } else {
            modifier.clickable(onClick = onClick)
        }

    Row(
        modifier = styledModifier,
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
