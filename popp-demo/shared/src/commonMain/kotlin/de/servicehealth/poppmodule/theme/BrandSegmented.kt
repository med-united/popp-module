package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SegmentedOption<T>(val id: T, val label: String)

@Composable
fun <T> BrandSegmented(
    options: List<SegmentedOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    fillMaxWidth: Boolean = true,
) {
    val c = BrandTheme.colors
    val trackBackground = if (dark) Color.White.copy(alpha = 0.08f) else c.mist

    val rowMod = (if (fillMaxWidth) modifier.fillMaxWidth() else modifier)
        .clip(CircleShape)
        .background(trackBackground)
        .padding(4.dp)

    Row(modifier = rowMod, verticalAlignment = Alignment.CenterVertically) {
        options.forEach { option ->
            val on = option.id == selected
            val segMod = (if (fillMaxWidth) Modifier.weight(1f) else Modifier)
                .clip(CircleShape)
                .background(
                    when {
                        on && dark -> c.white
                        on && !dark -> c.deep
                        else -> Color.Transparent
                    }
                )
                .clickable(
                    interactionSource = remember(option.id) { MutableInteractionSource() },
                    indication = null,
                    onClick = { onSelect(option.id) }
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)

            val labelColor = when {
                on && dark -> c.deep
                on && !dark -> c.white
                !on && dark -> Color.White.copy(alpha = 0.6f)
                else -> c.neutral700
            }

            Box(modifier = segMod, contentAlignment = Alignment.Center) {
                Text(
                    text = option.label,
                    color = labelColor,
                    style = TextStyle(
                        fontFamily = displayFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                )
            }
        }
    }
}

private val previewOptions = listOf(
    SegmentedOption("a", "Option A"),
    SegmentedOption("b", "Option B"),
    SegmentedOption("c", "Option C"),
)

@Preview @Composable private fun FirstSelectedBrandSegmentedPreview() {
    BrandSegmented(options = previewOptions, selected = "a", onSelect = {})
}

@Preview @Composable private fun MidSelectedBrandSegmentedPreview() {
    BrandSegmented(options = previewOptions, selected = "b", onSelect = {})
}

@Preview @Composable private fun DarkBrandSegmentedPreview() {
    BrandSegmented(options = previewOptions, selected = "a", onSelect = {}, dark = true)
}