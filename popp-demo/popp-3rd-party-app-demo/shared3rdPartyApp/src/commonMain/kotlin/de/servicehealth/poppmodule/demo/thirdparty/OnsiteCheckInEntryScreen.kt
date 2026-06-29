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
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.Institution
import de.servicehealth.poppmodule.demo.thirdparty.icon
import de.servicehealth.poppmodule.demo.thirdparty.label
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_favorites
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_favorites_empty_hint
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
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnsiteCheckInEntryScreen(
    onClose: () -> Unit,
    onSearchClick: () -> Unit,
    onQrScanClick: () -> Unit,
    favorites: List<Institution> = emptyList(),
    onFavoriteClick: (id: String, name: String, address: String, category: String) -> Unit = { _, _, _, _ -> },
) {
    val c = BrandTheme.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
    ) {
        BrandScreenHeader(title = stringResource(Res.string.checkin_entry_header), onClose = onClose)

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                BrandProgressDots(stepCount = 4, currentStep = 0)
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(Res.string.checkin_entry_question),
                color = c.ink,
                style = BrandTheme.typography.displaySmall,
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

            FavoritesSection(favorites = favorites, onFavoriteClick = onFavoriteClick)
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
                modifier =
                    Modifier
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
    favorites: List<Institution>,
    onFavoriteClick: (String, String, String, String) -> Unit,
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
            text = "${favorites.size} gespeichert",
            color = c.neutral700,
            style = BrandTheme.typography.bodySmall,
        )
    }

    Spacer(Modifier.height(10.dp))

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.checkin_entry_favorites_empty_hint),
                color = c.neutral700,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        BrandCard(
            raised = true,
            padding = PaddingValues(0.dp),
        ) {
            Column {
                favorites.forEachIndexed { index, institution ->
                    FavoriteRow(
                        id = institution.id,
                        icon = institution.type.icon(),
                        title = institution.name,
                        subtitle = institution.address,
                        category = institution.type.label,
                        onClick = onFavoriteClick,
                    )

                    if (index != favorites.lastIndex) {
                        HorizontalDivider(color = c.mist)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    id: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    category: String,
    onClick: (String, String, String, String) -> Unit,
) {
    val c = BrandTheme.colors

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick(id, title, subtitle, category) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
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

@Preview @Composable
private fun OnsiteCheckInEntryScreenPreview() {
    PreviewBrandTheme {
        OnsiteCheckInEntryScreen(
            onClose = {},
            onSearchClick = {},
            onQrScanClick = {},
        )
    }
}
