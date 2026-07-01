package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_choose_other
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_confirm_button
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_favorite_add
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_favorite_added
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_favorite_hint
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_header
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_label_address
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_label_hours
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.confirm_institution_title
import de.servicehealth.poppmodule.theme.BrandBackButton
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import org.jetbrains.compose.resources.stringResource

data class LeiData(
    val institutionType: String,
    val institutionTypeIcon: ImageVector = Icons.Rounded.LocalHospital,
    val name: String,
    val address: String,
    val openingHours: String,
)

val stubLeiData =
    mockInstitutions.first().let { institution ->
        LeiData(
            institutionType = institution.type.label,
            institutionTypeIcon = institution.type.icon(),
            name = institution.name,
            address = institution.address,
            openingHours = "Mo-Fr 8:00-18:30 · Sa 9:00-13:00",
        )
    }

/**
 * A3 - "Einrichtung bestaetigen"
 *
 * Displays VZD-looked-up LEI data and collects explicit user consent before
 * transmitting insured data. Gematik refs: A_28488, A_27621.
 *
 * @param leiData         Institution data to display (AC2). Use [stubLeiData] until POPPM-116.
 * @param currentStep     0-based index for BrandProgressDots (AC4).
 * @param totalSteps      Total steps in the check-in flow (AC4).
 * @param isFavorite      Whether [leiData]'s institution is currently saved as a favorite.
 * @param onToggleFavorite Toggles [leiData]'s institution in the favorites list.
 * @param onConfirm       Grants consent and triggers the auth flow (AC3 primary button).
 * @param onBack          Navigates to previous screen (AC4 back button).
 * @param onChooseOther   Returns to search/scanner (AC3 secondary button).
 * @param onClose         Closes the VOR-ORT-CHECK-IN flow entirely.
 */
@Composable
fun ConfirmInstitutionScreen(
    leiData: LeiData = stubLeiData,
    currentStep: Int = 1,
    totalSteps: Int = 4,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onChooseOther: () -> Unit,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
    ) {
        BrandScreenHeader(
            title = stringResource(Res.string.confirm_institution_header),
            onClose = onClose,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandBackButton(
                    label = stringResource(Res.string.confirm_institution_back),
                    onClick = onBack,
                )
                Spacer(Modifier.weight(1f))
                BrandProgressDots(stepCount = totalSteps, currentStep = currentStep)
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(Res.string.confirm_institution_title),
                color = c.ink,
                style = BrandTheme.typography.displaySmall,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.confirm_institution_subtitle),
                color = c.neutral700,
                style = BrandTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(24.dp))

            LeiCard(data = leiData)

            Spacer(Modifier.height(16.dp))

            FavoriteToggleRow(isFavorite = isFavorite, onToggle = onToggleFavorite)

            Spacer(Modifier.height(32.dp))
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandButton(
                text = stringResource(Res.string.confirm_institution_confirm_button),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                variant = BrandButtonVariant.Primary,
                size = BrandButtonSize.Lg,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = c.white,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.confirm_institution_choose_other),
                color = c.neutral700,
                style = BrandTheme.typography.labelLarge,
                modifier =
                    Modifier
                        .clickable(onClick = onChooseOther)
                        .padding(vertical = 13.dp),
            )
        }
    }
}

@Composable
private fun LeiCard(data: LeiData) {
    val c = BrandTheme.colors

    BrandCard(
        raised = true,
        padding = PaddingValues(0.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(
                            brush =
                                remember {
                                    Brush.linearGradient(colors = listOf(c.violet700, c.violet))
                                },
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Column {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = data.institutionTypeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = data.institutionType.uppercase(),
                            color = Color.White,
                            style = BrandTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = data.name,
                        color = Color.White,
                        style = BrandTheme.typography.displayMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LeiDetailRow(
                    icon = Icons.Rounded.LocationOn,
                    label = stringResource(Res.string.confirm_institution_label_address),
                    value = data.address,
                )
                LeiDetailRow(
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(Res.string.confirm_institution_label_hours),
                    value = data.openingHours,
                )
            }
        }
    }
}

@Composable
private fun LeiDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    val c = BrandTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(c.violet100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = c.violet,
                modifier = Modifier.size(19.dp),
            )
        }

        Column {
            Text(
                text = label,
                color = c.neutral700,
                style = BrandTheme.typography.labelSmall,
                letterSpacing = 0.6.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = c.ink,
                style = BrandTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun FavoriteToggleRow(
    isFavorite: Boolean,
    onToggle: () -> Unit,
) {
    val c = BrandTheme.colors
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (isFavorite) c.yellow100 else c.white)
                .border(1.5.dp, if (isFavorite) c.yellow else c.mist, shape)
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFavorite) c.yellow else c.mist),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (isFavorite) Color.White else c.neutral700,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text =
                    stringResource(
                        if (isFavorite) {
                            Res.string.confirm_institution_favorite_added
                        } else {
                            Res.string.confirm_institution_favorite_add
                        },
                    ),
                color = c.ink,
                style = BrandTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.confirm_institution_favorite_hint),
                color = c.neutral700,
                style = BrandTheme.typography.bodySmall,
            )
        }

        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isFavorite) c.success else Color.Transparent)
                    .border(1.5.dp, if (isFavorite) c.success else c.silver, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun LeiDetailRowPreview() {
    PreviewBrandTheme {
        LeiDetailRow(
            icon = Icons.Rounded.LocationOn,
            label = "ADRESSE",
            value = "Marktplatz 3, 52062 Aachen",
        )
    }
}

@Preview
@Composable
private fun ConfirmInstitutionScreenPreview() {
    ConfirmInstitutionScreen(
        onConfirm = {},
        onBack = {},
        onChooseOther = {},
        onClose = {},
    )
}

@Preview
@Composable
private fun ConfirmInstitutionScreenFavoritedPreview() {
    ConfirmInstitutionScreen(
        isFavorite = true,
        onConfirm = {},
        onBack = {},
        onChooseOther = {},
        onClose = {},
    )
}
