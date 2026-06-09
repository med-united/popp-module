package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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

@Preview @Composable private fun DefaultBrandSpinnerPreview() {
    val c = BrandColors()
    Box(Modifier.background(c.deep).padding(16.dp)) {
        BrandSpinner()
    }
}

@Preview @Composable private fun VioletBrandSpinnerPreview() {
    BrandSpinner(color = BrandColors().violet)
}

@Preview @Composable private fun LargeBrandSpinnerPreview() {
    BrandSpinner(size = 48.dp, strokeWidth = 5.dp, color = BrandColors().violet)
}