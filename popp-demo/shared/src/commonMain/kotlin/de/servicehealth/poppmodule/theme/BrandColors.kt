package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * service·health brand tokens, ported from the Claude Design handoff (`brand.jsx`).
 *
 * The palette is built around three signal colours — Electric Violet, Deep Space and
 * Signal Yellow — plus a neutral ramp and semantic accent colours.
 */
@Immutable
data class BrandColors(
    val yellow: Color = Color(0xFFF7EA5A),
    val yellow300: Color = Color(0xFFFAF2A7),
    val yellow100: Color = Color(0xFFFDFBEB),

    val violet: Color = Color(0xFF5F29EF),
    val violet700: Color = Color(0xFF350791),
    val violet300: Color = Color(0xFFAE8DF5),
    val violet100: Color = Color(0xFFEBE3FC),

    val deep: Color = Color(0xFF100030),

    val ink: Color = Color(0xFF18181B),
    val neutral700: Color = Color(0xFF5D5D63),
    val silver: Color = Color(0xFFC7C7CC),
    val mist: Color = Color(0xFFF2F2F7),
    val white: Color = Color(0xFFFFFFFF),

    val success: Color = Color(0xFF1F8A5B),
    val successBg: Color = Color(0xFFE4F3EC),
    val warning: Color = Color(0xFFC98A12),
    val warningBg: Color = Color(0xFFFBF0D8),
    val danger: Color = Color(0xFFD23B3B),
)

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(name, fontSize = 9.sp, lineHeight = 11.sp)
    }
}

@Composable
private fun PaletteGroup(label: String, swatches: List<Pair<String, Color>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            swatches.forEach { (name, color) -> ColorSwatch(name, color) }
        }
    }
}

@Preview
@Composable
fun brandColorPalettePreview() {
    val c = BrandColors()
    Column(
        modifier = Modifier.background(c.white).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PaletteGroup("Yellow", listOf("yellow" to c.yellow, "yellow300" to c.yellow300, "yellow100" to c.yellow100))
        PaletteGroup("Violet", listOf("violet700" to c.violet700, "violet" to c.violet, "violet300" to c.violet300, "violet100" to c.violet100))
        PaletteGroup("Neutral", listOf("deep" to c.deep, "ink" to c.ink, "neutral700" to c.neutral700, "silver" to c.silver, "mist" to c.mist, "white" to c.white))
        PaletteGroup("Semantic", listOf("success" to c.success, "successBg" to c.successBg, "warning" to c.warning, "warningBg" to c.warningBg, "danger" to c.danger))
    }
}

internal fun BrandColors.toMaterialColorScheme(): ColorScheme = lightColorScheme(
    primary = violet,
    onPrimary = white,
    primaryContainer = violet100,
    onPrimaryContainer = violet700,
    secondary = deep,
    onSecondary = white,
    secondaryContainer = mist,
    onSecondaryContainer = ink,
    tertiary = yellow,
    onTertiary = ink,
    tertiaryContainer = yellow300,
    onTertiaryContainer = ink,
    background = white,
    onBackground = ink,
    surface = white,
    onSurface = ink,
    surfaceVariant = mist,
    onSurfaceVariant = neutral700,
    outline = silver,
    outlineVariant = mist,
    error = danger,
    onError = white,
)
