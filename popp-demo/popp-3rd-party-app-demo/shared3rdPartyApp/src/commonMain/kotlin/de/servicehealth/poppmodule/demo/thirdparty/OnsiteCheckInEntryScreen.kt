package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_favorites
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_favorites_count
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_header
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_qr_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_qr_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_question
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_search_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_search_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_subtitle
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnsiteCheckInEntryScreen(
    onClose: () -> Unit,
    onSearchClick: () -> Unit,
    onQrScanClick: () -> Unit,
    onFavoriteClick: (name: String, address: String, category: String) -> Unit = { _, _, _ -> },
) {
    BrandTheme {
        val c = BrandTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
        ) {
            BrandScreenHeader(title = stringResource(Res.string.checkin_entry_header), onClose = onClose)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    BrandProgressDots(stepCount = 4, currentStep = 0)
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = stringResource(Res.string.checkin_entry_question),
                    color = c.ink,
                    style = BrandTheme.typography.displayMedium.copy(fontSize = 32.sp),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.checkin_entry_subtitle),
                    color = c.neutral700,
                    style = BrandTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Search,
                        title = stringResource(Res.string.checkin_entry_search_title),
                        subtitle = stringResource(Res.string.checkin_entry_search_subtitle),
                        onClick = onSearchClick,
                    )

                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.QrCodeScanner,
                        title = stringResource(Res.string.checkin_entry_qr_title),
                        subtitle = stringResource(Res.string.checkin_entry_qr_subtitle),
                        onClick = onQrScanClick,
                    )
                }

                Spacer(Modifier.height(20.dp))

                FavoritesSection(onFavoriteClick = onFavoriteClick)
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val c = BrandTheme.colors

    BrandCard(
        modifier = modifier.height(145.dp),
        onClick = onClick,
        raised = false,
        padding = PaddingValues(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.deep),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = c.yellow,
                    modifier = Modifier.size(25.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = title,
                color = c.ink,
                style = BrandTheme.typography.titleMedium,
                maxLines = 2,
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = c.neutral700,
                style = BrandTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun FavoritesSection(
    onFavoriteClick: (String, String, String) -> Unit,
) {
    val c = BrandTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.checkin_entry_favorites),
            color = c.neutral700,
            style = BrandTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = Icons.Rounded.StarBorder,
            contentDescription = null,
            tint = c.yellow,
            modifier = Modifier.size(15.dp),
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = stringResource(Res.string.checkin_entry_favorites_count),
            color = c.neutral700,
            style = BrandTheme.typography.bodySmall,
        )
    }

    Spacer(Modifier.height(10.dp))

    BrandCard(
        raised = true,
        padding = PaddingValues(0.dp),
    ) {
        Column {
            FavoriteRow(
                icon = Icons.Rounded.LocalHospital,
                title = "Apotheke am Markt",
                subtitle = "Marktplatz 3, 52062 Aachen",
                category = "Apotheke",
                onClick = onFavoriteClick,
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = c.mist,
            )

            FavoriteRow(
                icon = Icons.Rounded.MedicalServices,
                title = "Hausarztpraxis Dr. Brandt",
                subtitle = "Theaterstraße 18, 52062 Aachen",
                category = "Hausarztpraxis",
                onClick = onFavoriteClick,
            )
        }
    }
}

@Composable
private fun FavoriteRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    category: String,
    onClick: (String, String, String) -> Unit,
) {
    val c = BrandTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(title, subtitle, category) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(c.violet100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = c.violet,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = c.ink,
                    style = BrandTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                Spacer(Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Rounded.StarBorder,
                    contentDescription = null,
                    tint = c.yellow,
                    modifier = Modifier.size(14.dp),
                )
            }

            Text(
                text = subtitle,
                color = c.neutral700,
                style = BrandTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = c.silver,
            modifier = Modifier.size(20.dp),
        )
    }
}