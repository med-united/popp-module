package de.servicehealth.poppmodule.demo.thirdparty.can

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.egk_card_can_label
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.egk_card_sample_insurance
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.egk_card_sample_name
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.egk_card_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.egk_card_title
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

private const val SAMPLE_CAN = "123 456"

/** A brand-styled eGK card graphic; [highlightCan] frames the CAN to guide the user. */
@Composable
fun EgkCardGraphic(
    modifier: Modifier = Modifier,
    width: Dp = 260.dp,
    highlightCan: Boolean = true,
) {
    val c = BrandTheme.colors
    Box(
        modifier =
            modifier
                .size(width = width, height = width * 0.62f)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(c.violet, c.violet700, c.deep)))
                .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.egk_card_title),
                        color = c.white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = stringResource(Res.string.egk_card_subtitle),
                        color = c.white.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Contactless,
                    contentDescription = null,
                    tint = c.white.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 34.dp, height = 26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Brush.linearGradient(listOf(c.yellow, c.yellow300))),
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.egk_card_can_label),
                        color = if (highlightCan) c.yellow else c.white.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier =
                            if (highlightCan) {
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(c.yellow.copy(alpha = 0.22f))
                                    .border(1.5.dp, c.yellow, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            } else {
                                Modifier
                            },
                    ) {
                        Text(
                            text = SAMPLE_CAN,
                            color = c.white,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }

            Column {
                Text(
                    text = stringResource(Res.string.egk_card_sample_name),
                    color = c.white.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
                Text(
                    text = stringResource(Res.string.egk_card_sample_insurance),
                    color = c.white.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                )
            }
        }
    }
}
