package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandTheme

// ---------------------------------------------------------------------------
// Data model – stubbed for now, to be replaced with real VZD lookup data
// ---------------------------------------------------------------------------

data class LeiData(
    val institutionType: String,            // e.g. "APOTHEKE"
    val institutionTypeIcon: ImageVector = Icons.Rounded.LocalHospital,
    val name: String,                       // e.g. "Apotheke am Markt"
    val address: String,                    // e.g. "Marktplatz 3, 52062 Aachen"
    val openingHours: String,               // e.g. "Mo–Fr 8:00–18:30 · Sa 9:00–13:00"
)

// Stub – replace with real VZD result once POPPM-116 is implemented
val stubLeiData = LeiData(
    institutionType = "Apotheke",
    name = "Apotheke am Markt",
    address = "Marktplatz 3, 52062 Aachen",
    openingHours = "Mo–Fr 8:00–18:30 · Sa 9:00–13:00",
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * A3 – "Einrichtung bestätigen"
 *
 * Displays VZD-looked-up LEI data and collects explicit user consent before
 * transmitting insured data. Gematik refs: A_28488, A_27621.
 *
 * @param leiData       Institution data to display (AC2). Use [stubLeiData] until POPPM-116.
 * @param currentStep   1-based index for the progress dots (AC4).
 * @param totalSteps    Total steps in the check-in flow (AC4).
 * @param onConfirm     Grants consent and triggers the auth flow (AC3 primary button).
 * @param onChooseOther Returns to search/scanner (AC3 secondary button + back button).
 * @param onClose       Closes the VOR-ORT-CHECK-IN flow entirely.
 */
@Composable
fun ConfirmInstitutionScreen(
    leiData: LeiData = stubLeiData,
    currentStep: Int = 2,   // 0-based index for BrandProgressDots
    totalSteps: Int = 4,
    onConfirm: () -> Unit,
    onChooseOther: () -> Unit,
    onClose: () -> Unit,
) {
    BrandTheme {
        val c = BrandTheme.colors

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding()
        ) {
            // AC4 + top bar
            BrandScreenHeader(title = "VOR-ORT-CHECK-IN", onClose = onClose)

            // Scrollable body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
            ) {
                // AC4: back button + progress dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Zurück",
                            tint = c.violet,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Zurück",
                            color = c.violet,
                            style = BrandTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    BrandProgressDots(stepCount = totalSteps, currentStep = currentStep)
                }

                Spacer(Modifier.height(22.dp))

                // AC1: title + subtitle
                Text(
                    text = "Einrichtung bestätigen",
                    color = c.ink,
                    style = BrandTheme.typography.displayMedium.copy(fontSize = 32.sp),
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Wir haben diese Einrichtung erkannt. Bitte prüfen und bestätigen Sie.",
                    color = c.neutral700,
                    style = BrandTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(24.dp))

                // AC2: LEI card
                LeiCard(data = leiData)

                Spacer(Modifier.height(32.dp))
            }

            // AC3: action buttons – pinned to bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandButton(
                    text = "Bestätigen & anmelden",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    variant = BrandButtonVariant.Primary,
                    size = BrandButtonSize.Lg,
                    trailingIcon = {
                        Text(
                            text = "→",
                            color = c.white,
                            fontSize = 17.sp,
                        )
                    },
                )

                Spacer(Modifier.height(16.dp))

                BrandButton(
                    text = "Andere Einrichtung wählen",
                    onClick = onChooseOther,
                    variant = BrandButtonVariant.Ghost,
                    size = BrandButtonSize.Md,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// AC2: LEI card
// ---------------------------------------------------------------------------

@Composable
private fun LeiCard(data: LeiData) {
    val c = BrandTheme.colors

    BrandCard(
        raised = true,
        padding = PaddingValues(0.dp),
    ) {
        Column {
            // Purple gradient header: institution type badge + name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(c.violet700, c.violet),
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Column {
                    // Institution type badge
                    Row(
                        modifier = Modifier
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

                    // Institution name
                    Text(
                        text = data.name,
                        color = Color.White,
                        style = BrandTheme.typography.displayMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Address + opening hours
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LeiDetailRow(
                    icon = Icons.Rounded.LocationOn,
                    label = "ADRESSE",
                    value = data.address,
                )
                LeiDetailRow(
                    icon = Icons.Rounded.Schedule,
                    label = "ÖFFNUNGSZEITEN",
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
            modifier = Modifier
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