package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_active
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_done
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_headline
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_location
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_proof_hint
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_time_label
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnsiteCheckInSuccessScreen(
    onClose: () -> Unit,
    institutionName: String = "Apotheke am Markt",
    institutionCategory: String = "Apotheke",
    proofEpochSeconds: Long? = null,
) {
    val c = BrandTheme.colors

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                c.success.copy(alpha = 0.98f),
                                c.success700,
                            ),
                    ),
                )
                .safeContentPadding(),
    ) {
        val scale = (maxHeight / 844.dp).coerceIn(0.82f, 1.08f)
        val horizontalPadding = (13.dp * scale).coerceIn(11.dp, 18.dp)

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
                            ambientColor = c.success700.copy(alpha = 0.45f),
                            spotColor = c.success700.copy(alpha = 0.35f),
                        )
                        .clip(CircleShape)
                        .background(c.white),
                contentAlignment = Alignment.Center,
            ) {
                ThickCheckmark(
                    modifier = Modifier.size(s(58.dp)),
                    color = c.success,
                    strokeWidth = s(7.dp),
                )
            }

            Spacer(Modifier.height(s(38.dp)))

            Text(
                text = stringResource(Res.string.checkin_success_headline),
                color = c.white,
                style = BrandTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )

            Spacer(Modifier.height(s(14.dp)))

            Text(
                text = locationAnnotatedText(institutionName),
                color = c.white.copy(alpha = 0.88f),
                style = BrandTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(s(34.dp)))

            ProofCard(
                institutionName = institutionName,
                institutionCategory = institutionCategory,
                proofTime = proofEpochSeconds?.let { formatProofTime(it) } ?: "Heute · 9:41 Uhr",
                scale = scale,
            )

            Spacer(Modifier.height(s(24.dp)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = c.white.copy(alpha = 0.75f),
                    modifier = Modifier.size(s(17.dp)),
                )

                Spacer(Modifier.width(s(9.dp)))

                Text(
                    text = stringResource(Res.string.checkin_success_proof_hint),
                    color = c.white.copy(alpha = 0.75f),
                    style = BrandTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.weight(1f))

            BrandButton(
                text = stringResource(Res.string.checkin_success_done),
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                variant = BrandButtonVariant.Accent,
                size = BrandButtonSize.Lg,
                leadingIcon = {
                    ThickCheckmark(
                        modifier = Modifier.size(s(20.dp)),
                        color = c.ink,
                        strokeWidth = s(3.dp),
                    )
                },
            )

            Spacer(Modifier.height(s(28.dp)))
        }
    }
}

@Composable
private fun ProofCard(
    institutionName: String,
    institutionCategory: String,
    proofTime: String,
    scale: Float,
) {
    val c = BrandTheme.colors

    fun s(value: Dp): Dp = value * scale

    BrandCard(
        modifier = Modifier.fillMaxWidth(),
        raised = true,
        padding =
            PaddingValues(
                horizontal = s(18.dp),
                vertical = s(18.dp),
            ),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(s(52.dp))
                            .clip(RoundedCornerShape(s(14.dp)))
                            .background(c.violet100),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = c.violet,
                        modifier = Modifier.size(s(27.dp)),
                    )
                }

                Spacer(Modifier.width(s(14.dp)))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institutionName,
                        color = c.ink,
                        style = BrandTheme.typography.titleLarge,
                    )

                    Spacer(Modifier.height(s(3.dp)))

                    Text(
                        text = institutionCategory,
                        color = c.neutral700,
                        style = BrandTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.width(s(8.dp)))

                ActivePill(
                    text = stringResource(Res.string.checkin_success_active),
                    scale = scale,
                )
            }

            Spacer(Modifier.height(s(18.dp)))

            HorizontalDivider(color = c.mist)

            Spacer(Modifier.height(s(16.dp)))

            ProofRow(
                label = stringResource(Res.string.checkin_success_time_label),
                value = proofTime,
            )
        }
    }
}

@Composable
private fun ProofRow(
    label: String,
    value: String,
) {
    val c = BrandTheme.colors

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = c.neutral700,
            style = BrandTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = value,
            color = c.ink,
            style = BrandTheme.typography.titleMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.45f),
        )
    }
}

@Composable
private fun ThickCheckmark(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.53f),
            end = Offset(size.width * 0.43f, size.height * 0.74f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round,
        )

        drawLine(
            color = color,
            start = Offset(size.width * 0.43f, size.height * 0.74f),
            end = Offset(size.width * 0.80f, size.height * 0.28f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ActivePill(
    text: String,
    scale: Float,
) {
    val c = BrandTheme.colors

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(c.success.copy(alpha = 0.12f))
                .padding(
                    horizontal = (10.dp * scale).coerceIn(8.dp, 11.dp),
                    vertical = (5.dp * scale).coerceIn(4.dp, 6.dp),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size((7.dp * scale).coerceIn(6.dp, 8.dp))
                    .clip(CircleShape)
                    .background(c.success),
        )

        Spacer(Modifier.width((6.dp * scale).coerceIn(5.dp, 7.dp)))

        Text(
            text = text,
            color = c.success,
            style = BrandTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun locationAnnotatedText(
    institutionName: String,
): AnnotatedString {
    val locationText =
        stringResource(Res.string.checkin_success_location, institutionName)

    return buildAnnotatedString {
        val start = locationText.indexOf(institutionName)

        if (start < 0) {
            append(locationText)
        } else {
            append(locationText.substring(0, start))

            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(institutionName)
            }

            append(locationText.substring(start + institutionName.length))
        }
    }
}
