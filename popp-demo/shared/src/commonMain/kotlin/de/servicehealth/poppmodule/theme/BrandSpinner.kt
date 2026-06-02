package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BrandSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    color: Color = BrandTheme.colors.white,
    strokeWidth: Dp = 3.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        trackColor = color.copy(alpha = 0.25f),
        strokeWidth = strokeWidth,
    )
}