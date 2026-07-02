package de.servicehealth.poppmodule.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class BrandCardListPosition { Standalone, First, Middle, Last }

@Composable
fun BrandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    raised: Boolean = false,
    flat: Boolean = false,
    selected: Boolean = false,
    padding: PaddingValues = PaddingValues(18.dp),
    listPosition: BrandCardListPosition = BrandCardListPosition.Standalone,
    content: @Composable () -> Unit,
) {
    val c = BrandTheme.colors
    val r = 18.dp
    val shape =
        when (listPosition) {
            BrandCardListPosition.Standalone -> RoundedCornerShape(r)
            BrandCardListPosition.First -> RoundedCornerShape(topStart = r, topEnd = r)
            BrandCardListPosition.Middle -> RoundedCornerShape(0.dp)
            BrandCardListPosition.Last -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && onClick != null) 0.985f else 1f, label = "brand-card-scale")
    val elevation =
        when {
            flat -> 0.dp
            listPosition == BrandCardListPosition.Standalone -> if (raised) 12.dp else 3.dp
            else -> 0.dp
        }

    val background = if (selected) c.violet100 else c.white
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) c.violet else c.mist

    // Middle and Last items overlap the previous item's bottom border by 1dp so junctions
    // appear as a single divider line rather than a doubled border.
    val overlapModifier =
        when (listPosition) {
            BrandCardListPosition.Middle, BrandCardListPosition.Last -> Modifier.offset(y = (-1).dp)
            else -> Modifier
        }

    val shadowModifier =
        if (elevation > 0.dp) {
            Modifier.shadow(elevation, shape, ambientColor = c.deep, spotColor = c.deep)
        } else {
            Modifier
        }

    val base =
        modifier
            .then(overlapModifier)
            .scale(scale)
            .then(shadowModifier)
            .clip(shape)
            .background(background)
            .border(borderWidth, borderColor, shape)

    val outer =
        if (onClick != null) {
            base.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
        } else {
            base
        }

    Box(modifier = outer.padding(padding), content = { content() })
}

@Preview @Composable
private fun DefaultBrandCardPreview() {
    PreviewBrandTheme { BrandCard { Text("Default card") } }
}

@Preview @Composable
private fun RaisedBrandCardPreview() {
    PreviewBrandTheme { BrandCard(raised = true) { Text("Raised card") } }
}

@Preview @Composable
private fun SelectedBrandCardPreview() {
    PreviewBrandTheme { BrandCard(selected = true) { Text("Selected card") } }
}

@Preview @Composable
private fun ClickableBrandCardPreview() {
    PreviewBrandTheme { BrandCard(onClick = {}) { Text("Clickable card") } }
}

@Preview @Composable
private fun ListBrandCardFirstPreview() {
    PreviewBrandTheme { BrandCard(listPosition = BrandCardListPosition.First) { Text("First") } }
}

@Preview @Composable
private fun ListBrandCardMiddlePreview() {
    PreviewBrandTheme { BrandCard(listPosition = BrandCardListPosition.Middle) { Text("Middle") } }
}

@Preview @Composable
private fun ListBrandCardLastPreview() {
    PreviewBrandTheme { BrandCard(listPosition = BrandCardListPosition.Last) { Text("Last") } }
}
