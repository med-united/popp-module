package de.servicehealth.poppmodule.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun BrandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    raised: Boolean = false,
    selected: Boolean = false,
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit,
) {
    val c = BrandTheme.colors
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && onClick != null) 0.985f else 1f, label = "brand-card-scale")
    val elevation = if (raised) 12.dp else 3.dp

    val background = if (selected) c.violet100 else c.white
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) c.violet else c.mist

    val base = modifier
        .scale(scale)
        .shadow(elevation, shape, ambientColor = c.deep, spotColor = c.deep)
        .clip(shape)
        .background(background)
        .border(borderWidth, borderColor, shape)

    val outer = if (onClick != null) base.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    ) else base

    Box(modifier = outer.padding(padding), content = { content() })
}