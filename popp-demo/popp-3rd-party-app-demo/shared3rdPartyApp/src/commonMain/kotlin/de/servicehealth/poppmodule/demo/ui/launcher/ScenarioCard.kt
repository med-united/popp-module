package de.servicehealth.poppmodule.demo.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandTheme

/**
 * A single-select scenario tile. Wraps [BrandCard] with `selected` styling and a
 * violet check badge in the top-right when selected.
 */
@Composable
fun ScenarioCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BrandTheme.colors
    BrandCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        selected = selected,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = c.ink, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, color = c.neutral700, style = MaterialTheme.typography.bodyMedium)
            }
            if (selected) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(c.violet),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        color = c.white,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
