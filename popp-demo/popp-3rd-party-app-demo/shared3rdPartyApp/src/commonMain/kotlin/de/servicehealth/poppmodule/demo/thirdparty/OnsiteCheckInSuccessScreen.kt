package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_active
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_done
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_headline
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_location
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_proof_hint
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_proof_label
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_success_time_label
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandTag
import de.servicehealth.poppmodule.theme.BrandTagTone
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnsiteCheckInSuccessScreen(
    onClose: () -> Unit,
    institutionName: String = "Apotheke am Markt",
    institutionCategory: String = "Apotheke",
    proofId: String = "PoPP–MAR–7F2A9C",
    proofEpochSeconds: Long? = null,
) {
    val c = BrandTheme.colors

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.success700)
                .safeContentPadding(),
    ) {
        val scale = (maxHeight / 844.dp).coerceIn(0.82f, 1.08f)
        val horizontalPadding = (24.dp * scale).coerceIn(20.dp, 30.dp)

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
                        .clip(CircleShape)
                        .background(c.white),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = c.success,
                    modifier = Modifier.size(s(58.dp)),
                )
            }

            Spacer(Modifier.height(s(38.dp)))

            Text(
                text = stringResource(Res.string.checkin_success_headline),
                color = c.white,
                fontSize = (38f * scale).coerceIn(32f, 42f).sp,
                lineHeight = (42f * scale).coerceIn(36f, 46f).sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )

            Spacer(Modifier.height(s(14.dp)))

            Text(
                text = locationAnnotatedText(institutionName),
                color = c.white.copy(alpha = 0.88f),
                fontSize = (18f * scale).coerceIn(16f, 20f).sp,
                lineHeight = (27f * scale).coerceIn(23f, 30f).sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(s(34.dp)))

            ProofCard(
                institutionName = institutionName,
                institutionCategory = institutionCategory,
                proofId = proofId,
                proofTime = proofEpochSeconds?.let { formatProofTime(it) } ?: "Heute · 9:41 Uhr",
                scale = scale,
            )

            Spacer(Modifier.height(s(24.dp)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = c.white.copy(alpha = 0.75f),
                    modifier = Modifier.size(s(18.dp)),
                )

                Spacer(Modifier.width(s(10.dp)))

                Text(
                    text = stringResource(Res.string.checkin_success_proof_hint),
                    color = c.white.copy(alpha = 0.75f),
                    fontSize = (16f * scale).coerceIn(14f, 17f).sp,
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
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = c.ink,
                        modifier = Modifier.size(s(20.dp)),
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
    proofId: String,
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
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = c.violet,
                        modifier = Modifier.size(s(28.dp)),
                    )
                }

                Spacer(Modifier.width(s(14.dp)))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institutionName,
                        color = c.ink,
                        fontSize = (21f * scale).coerceIn(18f, 22f).sp,
                        lineHeight = (24f * scale).coerceIn(21f, 26f).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "$institutionCategory · $institutionCategory",
                        color = c.neutral700,
                        fontSize = (16f * scale).coerceIn(14f, 17f).sp,
                        lineHeight = (20f * scale).coerceIn(18f, 22f).sp,
                        maxLines = 1,
                    )
                }

                Spacer(Modifier.width(s(8.dp)))

                BrandTag(
                    text = stringResource(Res.string.checkin_success_active),
                    tone = BrandTagTone.Success,
                )
            }

            Spacer(Modifier.height(s(18.dp)))

            HorizontalDivider(color = c.mist)

            Spacer(Modifier.height(s(16.dp)))

            ProofRow(
                label = stringResource(Res.string.checkin_success_time_label),
                value = proofTime,
                scale = scale,
            )

            Spacer(Modifier.height(s(8.dp)))

            ProofRow(
                label = stringResource(Res.string.checkin_success_proof_label),
                value = proofId,
                scale = scale,
            )
        }
    }
}

@Composable
private fun ProofRow(
    label: String,
    value: String,
    scale: Float,
) {
    val c = BrandTheme.colors

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = c.neutral700,
            fontSize = (17f * scale).coerceIn(15f, 18f).sp,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = value,
            color = c.ink,
            fontSize = (17f * scale).coerceIn(15f, 18f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
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
