package de.servicehealth.poppmodule.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BrandButtonVariant { Primary, Secondary, Ghost, Accent, Dark }

enum class BrandButtonSize { Sm, Md, Lg }

private data class BrandButtonStyle(
    val background: Color,
    val content: Color,
    val borderColor: Color?,
    val elevation: Dp,
)

@Composable
private fun brandButtonStyle(
    variant: BrandButtonVariant,
    enabled: Boolean,
): BrandButtonStyle {
    val c = BrandTheme.colors
    return when (variant) {
        BrandButtonVariant.Primary -> BrandButtonStyle(
            background = if (enabled) c.violet else c.mist,
            content = if (enabled) c.white else c.silver,
            borderColor = null,
            elevation = if (enabled) 8.dp else 0.dp,
        )
        BrandButtonVariant.Secondary -> BrandButtonStyle(
            background = c.white,
            content = c.ink,
            borderColor = c.silver,
            elevation = 0.dp,
        )
        BrandButtonVariant.Ghost -> BrandButtonStyle(
            background = Color.Transparent,
            content = c.violet,
            borderColor = null,
            elevation = 0.dp,
        )
        BrandButtonVariant.Accent -> BrandButtonStyle(
            background = c.yellow,
            content = c.ink,
            borderColor = null,
            elevation = 0.dp,
        )
        BrandButtonVariant.Dark -> BrandButtonStyle(
            background = c.deep,
            content = c.white,
            borderColor = null,
            elevation = 0.dp,
        )
    }
}

@Composable
fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BrandButtonVariant = BrandButtonVariant.Primary,
    size: BrandButtonSize = BrandButtonSize.Md,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val style = brandButtonStyle(variant, enabled)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.97f else 1f, label = "brand-button-scale")

    val fontSize = when (size) {
        BrandButtonSize.Sm -> 14.sp
        BrandButtonSize.Md -> 15.5.sp
        BrandButtonSize.Lg -> 17.sp
    }
    val padding = when (size) {
        BrandButtonSize.Sm -> PaddingValues(horizontal = 16.dp, vertical = 9.dp)
        BrandButtonSize.Md -> PaddingValues(horizontal = 20.dp, vertical = 13.dp)
        BrandButtonSize.Lg -> PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    }

    Row(
        modifier = modifier
            .scale(scale)
            .let { if (style.elevation > 0.dp) it.shadow(style.elevation, CircleShape, ambientColor = BrandTheme.colors.violet, spotColor = BrandTheme.colors.violet) else it }
            .clip(CircleShape)
            .background(style.background)
            .let { m -> style.borderColor?.let { m.border(1.5.dp, it, CircleShape) } ?: m }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
    ) {
        CompositionLocalProvider(LocalContentColor provides style.content) {
            leadingIcon?.invoke()
            Text(
                text = text,
                color = style.content,
                style = TextStyle(
                    fontFamily = displayFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    letterSpacing = 0.15.sp,
                ),
            )
            trailingIcon?.invoke()
        }
    }
}

@Preview @Composable private fun SmallBrandButtonPreview() {
    BrandButton(text = "Small Button", size = BrandButtonSize.Sm, onClick = {})
}

@Preview @Composable private fun MediumBrandButtonPreview() {
    BrandButton(text = "Medium Button", onClick = {})
}

@Preview @Composable private fun LargeBrandButtonPreview() {
    BrandButton(text = "Large Button", size = BrandButtonSize.Lg, onClick = {})
}

