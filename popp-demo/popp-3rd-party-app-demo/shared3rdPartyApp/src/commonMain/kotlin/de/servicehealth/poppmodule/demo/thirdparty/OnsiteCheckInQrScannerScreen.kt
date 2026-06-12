package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.sdk.qr.ScanResult
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_instruction
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_searching
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_code_detected
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_invalid
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_scanner_close

@Composable
fun OnsiteCheckInQrScannerScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    BrandTheme {
        val c = BrandTheme.colors
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            c.deep,
                            c.deep,
                            c.deep.copy(alpha = 0.96f),
                        )
                    )
                )
                .safeContentPadding()
        ) {
            QrScannerHeader(
                onBack = onBack,
                onClose = onClose,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 32.dp)
                    .padding(top = 210.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.checkin_scanner_title),
                    color = c.white,
                    style = BrandTheme.typography.headlineLarge,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(Res.string.checkin_scanner_instruction),
                    color = c.white.copy(alpha = 0.76f),
                    style = BrandTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(36.dp))

                QrScanFrame(
                    succeeded = scanResult is ScanResult.Valid,
                    onResult = { scanResult = it },
                )

                Spacer(Modifier.height(28.dp))

                ScanStatus(result = scanResult)
            }
        }
    }
}

@Composable
private fun QrScannerHeader(
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(c.white.copy(alpha = 0.16f))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = stringResource(Res.string.checkin_scanner_back),
                tint = c.white,
                modifier = Modifier.size(16.dp),
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = stringResource(Res.string.checkin_scanner_back),
                color = c.white,
                style = BrandTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.weight(1f))

        BrandProgressDots(
            stepCount = 4,
            currentStep = 0,
            activeColor = c.white,
            inactiveColor = c.white.copy(alpha = 0.28f),
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
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
}

@Composable
private fun ScanStatus(result: ScanResult?) {
    val c = BrandTheme.colors
    when (result) {
        null -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BrandSpinner(size = 18.dp, color = c.yellow, strokeWidth = 2.5.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.checkin_scanner_searching),
                color = c.white.copy(alpha = 0.76f),
                style = BrandTheme.typography.bodyMedium,
            )
        }

        is ScanResult.Valid -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = c.success,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.checkin_scanner_code_detected),
                    color = c.success,
                    style = BrandTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = result.payload.telematikId +
                    (result.payload.workplaceId?.let { " · $it" } ?: ""),
                color = c.white.copy(alpha = 0.76f),
                style = BrandTheme.typography.bodyMedium,
            )
        }

        is ScanResult.Invalid -> Text(
            text = stringResource(Res.string.checkin_scanner_invalid),
            color = c.yellow,
            style = BrandTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QrScanFrame(
    succeeded: Boolean,
    onResult: (ScanResult) -> Unit,
) {
    val c = BrandTheme.colors

    var cameraActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(c.white.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        QrCameraViewfinder(
            onResult = onResult,
            onActiveChange = { cameraActive = it },
            modifier = Modifier.matchParentSize(),
        )

        if (cameraActive) {
            Icon(
                imageVector = Icons.Rounded.QrCode2,
                contentDescription = null,
                tint = c.white.copy(alpha = 0.36f),
                modifier = Modifier.size(150.dp),
            )
        }

        ScanCorners(color = if (succeeded) c.success else c.yellow)

        if (cameraActive && !succeeded) {
            ScanLine(color = c.yellow)
        }
    }
}

@Composable
private fun ScanCorners(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val inset = 2.dp.toPx()
        val w = size.width
        val h = size.height

        drawLine(color, Offset(inset, inset), Offset(cornerLength, inset), strokeWidth, StrokeCap.Square)
        drawLine(color, Offset(inset, inset), Offset(inset, cornerLength), strokeWidth, StrokeCap.Square)

        drawLine(color, Offset(w - cornerLength, inset), Offset(w - inset, inset), strokeWidth, StrokeCap.Square)
        drawLine(color, Offset(w - inset, inset), Offset(w - inset, cornerLength), strokeWidth, StrokeCap.Square)

        drawLine(color, Offset(inset, h - inset), Offset(cornerLength, h - inset), strokeWidth, StrokeCap.Square)
        drawLine(color, Offset(inset, h - cornerLength), Offset(inset, h - inset), strokeWidth, StrokeCap.Square)

        drawLine(color, Offset(w - cornerLength, h - inset), Offset(w - inset, h - inset), strokeWidth, StrokeCap.Square)
        drawLine(color, Offset(w - inset, h - cornerLength), Offset(w - inset, h - inset), strokeWidth, StrokeCap.Square)
    }
}

@Composable
private fun ScanLine(color: Color) {
    val transition = rememberInfiniteTransition(label = "qr-scan-line")
    val scanY = transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "qr-scan-line-y",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val y = h * scanY.value

        drawLine(
            color = color.copy(alpha = 0.85f),
            start = Offset(16.dp.toPx(), y),
            end = Offset(w - 16.dp.toPx(), y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0f),
                    color.copy(alpha = 0.24f),
                    color.copy(alpha = 0f),
                ),
                startY = y - 24.dp.toPx(),
                endY = y + 24.dp.toPx(),
            ),
            topLeft = Offset(0f, y - 24.dp.toPx()),
            size = Size(w, 48.dp.toPx()),
        )
    }
}