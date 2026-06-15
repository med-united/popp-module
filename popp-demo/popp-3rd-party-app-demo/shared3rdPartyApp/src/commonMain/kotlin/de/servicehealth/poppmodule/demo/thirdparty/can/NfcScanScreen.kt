package de.servicehealth.poppmodule.demo.thirdparty.can

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_header
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_stub_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_stub_title
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder for the eGK NFC scan step (POPPM-161). Establishes the route so the
 * CAN screen has a destination; POPPM-161 replaces the body.
 */
@Composable
fun NfcScanScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(c.white).safeContentPadding(),
    ) {
        BrandScreenHeader(
            title = stringResource(Res.string.checkin_entry_header),
            onClose = onClose,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(c.mist)
                        .clickable(onClick = onBack)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(Res.string.can_back),
                    tint = c.neutral700,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = stringResource(Res.string.can_back),
                    color = c.neutral700,
                    style = BrandTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.weight(1f))
            BrandProgressDots(stepCount = 4, currentStep = 3)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.nfc_stub_title),
                color = c.ink,
                style = BrandTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.nfc_stub_subtitle),
                color = c.neutral700,
                style = BrandTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
