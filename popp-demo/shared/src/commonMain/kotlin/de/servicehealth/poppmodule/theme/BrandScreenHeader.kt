package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrandScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
) {
    val c = BrandTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "service·health",
                color = c.violet,
                style =
                    BrandTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 14.dp)
                        .width(1.dp)
                        .height(22.dp)
                        .background(c.silver),
            )

            Text(
                text = title,
                color = c.neutral700,
                style =
                    BrandTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (onClose != null) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(c.mist)
                            .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Schließen",
                        tint = c.neutral700,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        HorizontalDivider(
            color = c.mist,
            thickness = 1.dp,
        )
    }
}

@Preview
@Composable
private fun BrandScreenHeaderPreview() {
    PreviewBrandTheme { BrandScreenHeader(title = "Versicherungsnachweis") }
}

@Preview
@Composable
private fun BrandScreenHeaderWithClosePreview() {
    PreviewBrandTheme { BrandScreenHeader(title = "Versicherungsnachweis", onClose = {}) }
}

@Preview
@Composable
private fun BrandScreenHeaderLongTitlePreview() {
    PreviewBrandTheme { BrandScreenHeader(title = "Ein sehr langer Titel der garantiert abgeschnitten wird", onClose = {}) }
}
