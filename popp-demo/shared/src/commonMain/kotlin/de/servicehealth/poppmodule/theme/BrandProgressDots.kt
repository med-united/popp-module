package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BrandProgressDots(
    stepCount: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
    inactiveColor: Color? = null,
) {
    val c = BrandTheme.colors
    val active = activeColor ?: c.violet
    val inactive = inactiveColor ?: c.silver

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(stepCount) { index ->
            if (index > 0) Spacer(Modifier.width(7.dp))
            Box(
                Modifier
                    .size(width = if (index == currentStep) 32.dp else 10.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(if (index <= currentStep) active else inactive),
            )
        }
    }
}

@Preview
@Composable
private fun FirstStepProgressDotsPreview() {
    BrandProgressDots(stepCount = 4, currentStep = 0)
}

@Preview
@Composable
private fun MidStepProgressDotsPreview() {
    BrandProgressDots(stepCount = 4, currentStep = 2)
}

@Preview
@Composable
private fun LastStepProgressDotsPreview() {
    BrandProgressDots(stepCount = 4, currentStep = 3)
}
