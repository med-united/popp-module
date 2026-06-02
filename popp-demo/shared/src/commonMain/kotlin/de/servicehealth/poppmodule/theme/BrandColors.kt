package de.servicehealth.poppmodule.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

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
