package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_action_can
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_action_close
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_action_retry
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_error_code_label
import de.servicehealth.poppmodule.demo.thirdparty.nfc.NfcErrorRecovery
import de.servicehealth.poppmodule.demo.thirdparty.nfc.NfcScanFailure
import de.servicehealth.poppmodule.demo.thirdparty.nfc.toErrorUi
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnsiteCheckInErrorScreen(
    failure: NfcScanFailure,
    code: String? = null,
    onRetry: () -> Unit,
    onReenterCan: () -> Unit,
    onClose: () -> Unit,
) {
    val c = BrandTheme.colors
    val ui = failure.toErrorUi()

    fun action(recovery: NfcErrorRecovery): () -> Unit =
        when (recovery) {
            NfcErrorRecovery.RETRY -> onRetry
            NfcErrorRecovery.REENTER_CAN -> onReenterCan
            NfcErrorRecovery.CLOSE -> onClose
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                c.danger.copy(alpha = 0.98f),
                                c.danger700,
                            ),
                    ),
                )
                .safeContentPadding(),
    ) {
        val scale = (maxHeight / 844.dp).coerceIn(0.82f, 1.08f)
        val horizontalPadding = (24.dp * scale).coerceIn(20.dp, 32.dp)

        fun s(value: Dp): Dp = value * scale

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(s(54.dp)))

            Box(
                modifier =
                    Modifier
                        .size(s(112.dp))
                        .shadow(
                            elevation = s(26.dp),
                            shape = CircleShape,
                            ambientColor = c.danger700.copy(alpha = 0.45f),
                            spotColor = c.danger700.copy(alpha = 0.35f),
                        )
                        .clip(CircleShape)
                        .background(c.white),
                contentAlignment = Alignment.Center,
            ) {
                ThickCross(
                    modifier = Modifier.size(s(50.dp)),
                    color = c.danger,
                    strokeWidth = s(7.dp),
                )
            }

            Spacer(Modifier.height(s(34.dp)))

            Text(
                text = stringResource(ui.titleRes),
                color = c.white,
                style = BrandTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("error_title"),
            )

            Spacer(Modifier.height(s(14.dp)))

            Text(
                text = stringResource(ui.messageRes),
                color = c.white.copy(alpha = 0.88f),
                style = BrandTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            if (code != null) {
                Spacer(Modifier.height(s(18.dp)))
                Text(
                    text = "${stringResource(Res.string.checkin_error_code_label)} · $code",
                    color = c.white.copy(alpha = 0.7f),
                    style = BrandTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("error_code"),
                )
            }

            Spacer(Modifier.weight(1f))

            BrandButton(
                text = recoveryLabel(ui.primary),
                onClick = action(ui.primary),
                modifier = Modifier.fillMaxWidth().testTag("error_primary"),
                variant = BrandButtonVariant.Accent,
                size = BrandButtonSize.Lg,
            )

            ui.secondary?.let { secondary ->
                Spacer(Modifier.height(s(6.dp)))
                Text(
                    text = recoveryLabel(secondary),
                    color = c.white,
                    style = BrandTheme.typography.labelLarge,
                    modifier =
                        Modifier
                            .clickable(onClick = action(secondary))
                            .padding(vertical = s(13.dp))
                            .testTag("error_secondary"),
                )
            }

            Spacer(Modifier.height(s(20.dp)))
        }
    }
}

@Composable
private fun recoveryLabel(recovery: NfcErrorRecovery): String =
    when (recovery) {
        NfcErrorRecovery.RETRY -> stringResource(Res.string.checkin_error_action_retry)
        NfcErrorRecovery.REENTER_CAN -> stringResource(Res.string.checkin_error_action_can)
        NfcErrorRecovery.CLOSE -> stringResource(Res.string.checkin_error_action_close)
    }

@Composable
private fun ThickCross(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
) {
    Canvas(modifier = modifier) {
        val pad = size.minDimension * 0.18f
        drawLine(
            color = color,
            start = Offset(pad, pad),
            end = Offset(size.width - pad, size.height - pad),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width - pad, pad),
            end = Offset(pad, size.height - pad),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Preview
@Composable
private fun OnsiteCheckInErrorScreenWrongCanPreview() {
    PreviewBrandTheme {
        OnsiteCheckInErrorScreen(
            failure = NfcScanFailure.WRONG_CAN,
            onRetry = {},
            onReenterCan = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun OnsiteCheckInErrorScreenServerPreview() {
    PreviewBrandTheme {
        OnsiteCheckInErrorScreen(
            failure = NfcScanFailure.SERVER_REJECTED,
            code = "WarningUnknownCertificates",
            onRetry = {},
            onReenterCan = {},
            onClose = {},
        )
    }
}
