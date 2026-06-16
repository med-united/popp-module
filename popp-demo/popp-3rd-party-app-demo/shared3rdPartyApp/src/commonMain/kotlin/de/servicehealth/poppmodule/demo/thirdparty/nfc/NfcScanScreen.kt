package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.demo.thirdparty.can.LocalCanStore
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_header
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_footer
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_reading
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_secure_transfer
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_unsupported
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_waiting
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Stateful eGK NFC scan screen (POPPM-161). Loads the CAN from [LocalCanStore], enables the NFC
 * reader via [rememberEgkChannelSource], runs `checkInWithEgk` through [NfcCheckInController], and
 * navigates on the terminal outcome. The reader is disabled when the screen leaves composition.
 */
@Composable
fun NfcScanScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSuccess: (poppToken: String, pruefnachweis: String) -> Unit,
    onError: (NfcScanFailure, String?) -> Unit,
) {
    val sdk = LocalPoppSdk.current
    val canStore = LocalCanStore.current
    val source = rememberEgkChannelSource()
    val scope = rememberCoroutineScope()
    val controller =
        remember(source) {
            NfcCheckInController(
                source = source,
                runner = CheckInRunner { channel, onProgress -> sdk.checkInWithEgk(channel, onProgress) },
                scope = scope,
            )
        }

    val state by controller.state.collectAsState()
    var can by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { can = canStore.load() }

    DisposableEffect(can) {
        can?.let { controller.start(it) }
        // Only stop if we actually started (skips a spurious disableReaderMode on the null→CAN load).
        onDispose { if (can != null) controller.stop() }
    }

    NfcOutcomeDispatcher(state = state, onSuccess = onSuccess, onError = onError)
    NfcScanContent(state = state, supported = source.isSupported, onBack = onBack, onClose = onClose)
}

/** Fires [onSuccess]/[onError] exactly once when [state] reaches a terminal outcome. */
@Composable
fun NfcOutcomeDispatcher(
    state: NfcScanUiState,
    onSuccess: (poppToken: String, pruefnachweis: String) -> Unit,
    onError: (NfcScanFailure, String?) -> Unit,
) {
    LaunchedEffect(state) {
        when (state) {
            is NfcScanUiState.Succeeded -> onSuccess(state.poppToken, state.pruefnachweis)
            is NfcScanUiState.Failed -> onError(state.reason, state.detail)
            else -> Unit
        }
    }
}

/** Stateless render of the NFC scan screen for a given [state]. */
@Composable
fun NfcScanContent(
    state: NfcScanUiState,
    supported: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors
    val statusText =
        when {
            !supported -> stringResource(Res.string.nfc_unsupported)
            state is NfcScanUiState.Reading -> stringResource(Res.string.nfc_reading)
            else -> stringResource(Res.string.nfc_waiting)
        }

    Column(
        modifier = Modifier.fillMaxSize().background(c.deep).safeContentPadding(),
    ) {
        BrandScreenHeader(title = stringResource(Res.string.checkin_entry_header), onClose = onClose)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(c.white.copy(alpha = 0.12f))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = stringResource(Res.string.can_back),
                    tint = c.white,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(stringResource(Res.string.can_back), color = c.white, style = BrandTheme.typography.labelLarge)
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
                text = stringResource(Res.string.nfc_title),
                color = c.white,
                style = BrandTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = statusText,
                color = c.white.copy(alpha = 0.75f),
                style = BrandTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("nfc_status"),
            )

            Spacer(Modifier.height(40.dp))
            NfcPulseRing()
            Spacer(Modifier.weight(1f))

            if (state is NfcScanUiState.Reading) {
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(c.white.copy(alpha = 0.12f))
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandSpinner(size = 16.dp, color = c.white)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(Res.string.nfc_secure_transfer, state.percent),
                        color = c.white,
                        style = BrandTheme.typography.labelLarge,
                        modifier = Modifier.testTag("nfc_percent"),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = stringResource(Res.string.nfc_footer),
                color = c.white.copy(alpha = 0.5f),
                style = BrandTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NfcPulseRing() {
    val c = BrandTheme.colors
    val transition = rememberInfiniteTransition(label = "nfc-pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier
                    .size(150.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(c.violet.copy(alpha = 0.25f)),
        )
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(c.violet.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Contactless,
                contentDescription = null,
                tint = c.white,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}
