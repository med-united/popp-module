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
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OnsiteCheckInQrScannerScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    BrandTheme {
        val c = BrandTheme.colors

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
                    text = "QR-Code scannen",
                    color = c.white,
                    style = BrandTheme.typography.headlineLarge,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Richten Sie die Kamera auf den Check-in-\nCode der Einrichtung.",
                    color = c.white.copy(alpha = 0.76f),
                    style = BrandTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(36.dp))

                QrScanFrame()

                Spacer(Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    BrandSpinner(
                        size = 18.dp,
                        color = c.yellow,
                        strokeWidth = 2.5.dp,
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Suche nach Code...",
                        color = c.white.copy(alpha = 0.76f),
                        style = BrandTheme.typography.bodyMedium,
                    )
                }
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
                contentDescription = "Zurück",
                tint = c.white,
                modifier = Modifier.size(16.dp),
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text = "Zurück",
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
                contentDescription = "Schließen",
                tint = c.white,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun QrScanFrame() {
    val c = BrandTheme.colors

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

    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(c.white.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.QrCode2,
            contentDescription = null,
            tint = c.white.copy(alpha = 0.36f),
            modifier = Modifier.size(150.dp),
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val cornerLength = 40.dp.toPx()
            val inset = 2.dp.toPx()
            val w = size.width
            val h = size.height

            drawLine(c.yellow, Offset(inset, inset), Offset(cornerLength, inset), strokeWidth, StrokeCap.Square)
            drawLine(c.yellow, Offset(inset, inset), Offset(inset, cornerLength), strokeWidth, StrokeCap.Square)

            drawLine(c.yellow, Offset(w - cornerLength, inset), Offset(w - inset, inset), strokeWidth, StrokeCap.Square)
            drawLine(c.yellow, Offset(w - inset, inset), Offset(w - inset, cornerLength), strokeWidth, StrokeCap.Square)

            drawLine(c.yellow, Offset(inset, h - inset), Offset(cornerLength, h - inset), strokeWidth, StrokeCap.Square)
            drawLine(c.yellow, Offset(inset, h - cornerLength), Offset(inset, h - inset), strokeWidth, StrokeCap.Square)

            drawLine(c.yellow, Offset(w - cornerLength, h - inset), Offset(w - inset, h - inset), strokeWidth, StrokeCap.Square)
            drawLine(c.yellow, Offset(w - inset, h - cornerLength), Offset(w - inset, h - inset), strokeWidth, StrokeCap.Square)

            val y = h * scanY.value
            drawLine(
                color = c.yellow.copy(alpha = 0.85f),
                start = Offset(16.dp.toPx(), y),
                end = Offset(w - 16.dp.toPx(), y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        c.yellow.copy(alpha = 0f),
                        c.yellow.copy(alpha = 0.24f),
                        c.yellow.copy(alpha = 0f),
                    ),
                    startY = y - 24.dp.toPx(),
                    endY = y + 24.dp.toPx(),
                ),
                topLeft = Offset(0f, y - 24.dp.toPx()),
                size = Size(w, 48.dp.toPx()),
            )
        }
    }
}