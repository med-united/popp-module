package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BrandTagTone { Violet, Solid, Yellow, Success, Warning, Neutral, OnDark }

private data class TagPalette(val background: Color, val content: Color)

@Composable
private fun tagPalette(tone: BrandTagTone): TagPalette {
    val c = BrandTheme.colors
    return when (tone) {
        BrandTagTone.Violet -> TagPalette(c.violet100, c.violet700)
        BrandTagTone.Solid -> TagPalette(c.violet, c.white)
        BrandTagTone.Yellow -> TagPalette(c.yellow300, c.ink)
        BrandTagTone.Success -> TagPalette(c.successBg, c.success)
        BrandTagTone.Warning -> TagPalette(c.warningBg, c.warning)
        BrandTagTone.Neutral -> TagPalette(c.mist, c.neutral700)
        BrandTagTone.OnDark -> TagPalette(Color.White.copy(alpha = 0.14f), c.white)
    }
}

@Composable
fun BrandTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: BrandTagTone = BrandTagTone.Violet,
    dot: Boolean = false,
) {
    val palette = tagPalette(tone)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(palette.background)
            .padding(
                start = if (dot) 8.dp else 10.dp,
                end = 10.dp,
                top = 4.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dot) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(palette.content)
            )
        }
        Text(
            text = text.uppercase(),
            color = palette.content,
            style = TextStyle(
                fontFamily = displayFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.7.sp,
            ),
        )
    }
}

@Preview @Composable private fun TonesBrandTagPreview() {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrandTag(text = "Violet", tone = BrandTagTone.Violet)
        BrandTag(text = "Solid", tone = BrandTagTone.Solid)
        BrandTag(text = "Yellow", tone = BrandTagTone.Yellow)
        BrandTag(text = "Success", tone = BrandTagTone.Success)
        BrandTag(text = "Warning", tone = BrandTagTone.Warning)
        BrandTag(text = "Neutral", tone = BrandTagTone.Neutral)
    }
}

@Preview @Composable private fun OnDarkBrandTagPreview() {
    Box(Modifier.background(BrandColors().deep).padding(12.dp)) {
        BrandTag(text = "On Dark", tone = BrandTagTone.OnDark)
    }
}

@Preview @Composable private fun DotBrandTagPreview() {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrandTag(text = "Active", tone = BrandTagTone.Success, dot = true)
        BrandTag(text = "Warning", tone = BrandTagTone.Warning, dot = true)
        BrandTag(text = "Label", tone = BrandTagTone.Violet, dot = true)
    }
}