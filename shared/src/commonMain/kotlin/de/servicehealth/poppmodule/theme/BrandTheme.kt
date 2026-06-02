package de.servicehealth.poppmodule.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * The service·health corner radii. Cards use 18dp, fields use 14dp, buttons are
 * fully pill-shaped — Material maps those into its [Shapes] slots as closely as
 * possible.
 */
private val brandShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * CompositionLocal that exposes the raw [BrandColors] palette to brand primitives
 * (Tag tones, success/warning surfaces, etc.) that need colours outside the
 * Material3 [androidx.compose.material3.ColorScheme].
 */
val LocalBrandColors = staticCompositionLocalOf { BrandColors() }

object BrandTheme {
    val colors: BrandColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandColors.current
}

@Composable
fun BrandTheme(
    colors: BrandColors = BrandColors(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBrandColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = brandTypography(),
            shapes = brandShapes,
            content = content,
        )
    }
}