package de.servicehealth.poppmodule.demo.thirdparty.nfc

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.demo.thirdparty.can.LocalCanStore
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_close
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_active
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_footer
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_reading
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_secure_transfer
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_success
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_title_done
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_title_unsupported
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_unsupported
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_verified
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.nfc_waiting
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val ACTIVE_LABEL_MILLIS = 1000L

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

    LifecycleResumeEffect(can) {
        val started = source.isSupported && can != null
        if (started) can?.let { controller.start(it) }
        onPauseOrDispose { if (started) controller.stop() }
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

    val reading = state is NfcScanUiState.Reading
    val completed = state is NfcScanUiState.Succeeded || (state as? NfcScanUiState.Reading)?.percent == 100
    val percent = if (completed) 100 else (state as? NfcScanUiState.Reading)?.percent ?: 0

    val statusText =
        when {
            !supported -> stringResource(Res.string.nfc_unsupported)
            completed -> stringResource(Res.string.nfc_success)
            reading -> stringResource(Res.string.nfc_reading)
            else -> stringResource(Res.string.nfc_waiting)
        }

    var showActiveLabel by remember { mutableStateOf(true) }
    LaunchedEffect(reading) {
        showActiveLabel = true
        if (reading) {
            delay(ACTIVE_LABEL_MILLIS)
            showActiveLabel = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(c.deep),
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    c.violet.copy(alpha = 0.55f),
                                    c.violet.copy(alpha = 0.20f),
                                    c.deep,
                                ),
                            radius = 1100f,
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeContentPadding(),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 12.dp),
            ) {
                if (!reading && !completed) {
                    Row(
                        modifier =
                            Modifier
                                .align(Alignment.CenterStart)
                                .clip(CircleShape)
                                .background(c.white.copy(alpha = 0.16f))
                                .clickable(onClick = onBack)
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(Res.string.can_back),
                            tint = c.white,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            stringResource(Res.string.can_back),
                            color = c.white,
                            style = BrandTheme.typography.labelLarge,
                        )
                    }
                }

                BrandProgressDots(
                    stepCount = 4,
                    currentStep = 3,
                    activeColor = c.white,
                    inactiveColor = c.white.copy(alpha = 0.28f),
                    modifier = Modifier.align(Alignment.Center),
                )

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(c.white.copy(alpha = 0.16f))
                            .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.checkin_scanner_close),
                        tint = c.white,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text =
                                when {
                                    !supported -> stringResource(Res.string.nfc_title_unsupported)
                                    completed -> stringResource(Res.string.nfc_title_done)
                                    else -> stringResource(Res.string.nfc_title)
                                },
                            color = c.white,
                            style = BrandTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = statusText,
                            color = c.white.copy(alpha = 0.75f),
                            style = BrandTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("nfc_status"),
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    val ringSize =
                        minOf(maxWidth * 0.78f, maxHeight * 0.82f, 220.dp).coerceAtLeast(120.dp)
                    NfcVisual(
                        percent = percent,
                        animate = supported && !completed,
                        completed = completed,
                        diameter = ringSize,
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (reading && !completed) {
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
                                        text =
                                            if (showActiveLabel) {
                                                stringResource(Res.string.nfc_active)
                                            } else {
                                                stringResource(
                                                    Res.string.nfc_secure_transfer,
                                                    percent,
                                                )
                                            },
                                        color = c.white,
                                        style = BrandTheme.typography.labelLarge,
                                        modifier = Modifier.testTag("nfc_percent"),
                                    )
                                }
                            } else if (completed) {
                                Row(
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(c.white.copy(alpha = 0.12f))
                                            .padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = c.yellow,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(Res.string.nfc_verified),
                                        color = c.white,
                                        style = BrandTheme.typography.labelLarge,
                                        modifier = Modifier.testTag("nfc_verified"),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(Res.string.nfc_footer),
                            color = c.white.copy(alpha = 0.5f),
                            style = BrandTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NfcVisual(
    percent: Int,
    animate: Boolean,
    completed: Boolean,
    modifier: Modifier = Modifier,
    diameter: Dp = 220.dp,
) {
    val c = BrandTheme.colors
    val trackColor = c.white.copy(alpha = 0.12f)
    val ringColor = if (completed) c.success else c.violet
    // All inner elements scale with [diameter] (proportions from the 220.dp reference design).
    val discSize = diameter * (170f / 220f)
    val ringSize = diameter * (176f / 220f)
    val ringStroke = diameter * (6f / 220f)
    val centerSize = diameter * (150f / 220f)
    val iconSize = diameter * (60f / 220f)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        // Radiating "sonar" waves — three rings expanding outward and fading, staggered.
        if (animate) {
            val transition = rememberInfiniteTransition(label = "nfc-waves")
            repeat(3) { i ->
                val t by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(2400, easing = LinearEasing),
                            initialStartOffset = StartOffset(i * 800),
                        ),
                    label = "wave$i",
                )
                Box(
                    modifier =
                        Modifier
                            .size(diameter)
                            .scale(0.42f + t * 0.58f)
                            .alpha((1f - t) * 0.5f)
                            .border(2.dp, c.violet300, CircleShape),
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .size(discSize)
                    .clip(CircleShape)
                    .background(c.violet.copy(alpha = 0.10f)),
        )
        Canvas(modifier = Modifier.size(ringSize)) {
            val stroke = ringStroke.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * (percent.coerceIn(0, 100) / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Box(
            modifier =
                Modifier
                    .size(centerSize)
                    .clip(CircleShape)
                    .background(
                        if (completed) c.success else c.white.copy(alpha = 0.06f),
                    )
                    .border(
                        width = if (completed) 0.dp else 2.dp,
                        color = if (completed) c.success else c.white.copy(alpha = 0.15f),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (completed) Icons.Rounded.Check else Icons.Outlined.Contactless,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = c.white.copy(alpha = 0.85f),
            )
        }
    }
}

@Preview
@Composable
private fun NfcScanWaitingPreview() =
    PreviewBrandTheme {
        NfcScanContent(state = NfcScanUiState.WaitingForCard, supported = true, onBack = {}, onClose = {})
    }

@Preview
@Composable
private fun NfcScanReadingPreview() =
    PreviewBrandTheme {
        NfcScanContent(state = NfcScanUiState.Reading(42), supported = true, onBack = {}, onClose = {})
    }

@Preview
@Composable
private fun NfcScanUnsupportedPreview() =
    PreviewBrandTheme {
        NfcScanContent(state = NfcScanUiState.WaitingForCard, supported = false, onBack = {}, onClose = {})
    }
