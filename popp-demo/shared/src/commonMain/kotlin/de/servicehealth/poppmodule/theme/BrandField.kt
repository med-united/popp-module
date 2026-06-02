package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text

@Composable
fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable (focused: Boolean) -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
) {
    val c = BrandTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (focused) c.violet else c.silver

    val focusedHalo = if (focused) Modifier.shadow(elevation = 6.dp, shape = shape, ambientColor = c.violet, spotColor = c.violet) else Modifier

    Row(
        modifier = modifier
            .then(focusedHalo)
            .clip(shape)
            .background(c.white)
            .border(1.5.dp, borderColor, shape)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        leadingIcon?.invoke(focused)
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = c.silver,
                    style = TextStyle(
                        fontFamily = bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                readOnly = readOnly,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(c.violet),
                textStyle = TextStyle(
                    fontFamily = bodyFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = c.ink,
                ),
            )
        }
    }
}