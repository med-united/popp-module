package de.servicehealth.poppmodule.demo.thirdparty.can

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_card_hint
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_change
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_continue
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_subtitle
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.can_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.checkin_entry_header
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val AUTO_ADVANCE_DELAY_MS = 450L

/**
 * CAN input mask: 6-digit entry via a custom keypad, encrypted single-CAN remember,
 * and hand-off to the NFC step. Fresh entry auto-advances on the 6th digit; a
 * pre-filled (remembered) CAN waits for explicit confirmation.
 */
@Composable
fun CanInputScreen(
    onBack: () -> Unit,
    onClose: () -> Unit,
    onComplete: () -> Unit,
) {
    val c = BrandTheme.colors
    val canStore = LocalCanStore.current

    var state by remember { mutableStateOf(CanInputState()) }
    var prefilled by remember { mutableStateOf(false) }
    var navigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = canStore.load()
        if (stored != null && isValidCan(stored)) {
            state = CanInputState(stored)
            prefilled = true
        }
    }

    // Fresh entry: persist + auto-advance once the 6th digit is typed.
    // NOTE: `navigated` must NOT be a key here. It is written inside the effect, so keying
    // on it would cancel this coroutine mid-`delay` before `onComplete()` runs. The inner
    // `!navigated` check still guards against re-entry.
    LaunchedEffect(state.isComplete, prefilled) {
        if (state.isComplete && !prefilled && !navigated) {
            navigated = true
            canStore.save(state.digits)
            delay(AUTO_ADVANCE_DELAY_MS)
            onComplete()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
    ) {
        BrandScreenHeader(
            title = stringResource(Res.string.checkin_entry_header),
            onClose = onClose,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 14.dp),
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

            BrandProgressDots(stepCount = 4, currentStep = 2)
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.can_title),
                color = c.ink,
                style = BrandTheme.typography.displayMedium.copy(fontSize = 28.sp),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.can_subtitle),
                color = c.neutral700,
                style = BrandTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            EgkCardGraphic(highlightCan = true)

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = c.neutral700,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = stringResource(Res.string.can_card_hint),
                    color = c.neutral700,
                    style = BrandTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(20.dp))

            CanDigitBoxes(digits = state.digits, masked = prefilled)

            Spacer(Modifier.height(24.dp))

            if (prefilled) {
                BrandButton(
                    text = stringResource(Res.string.can_continue),
                    onClick = {
                        if (!navigated) {
                            navigated = true
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("can_continue"),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(Res.string.can_change),
                    color = c.violet,
                    style = BrandTheme.typography.labelLarge,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .clickable {
                                state = state.cleared()
                                prefilled = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("can_change"),
                )
            } else {
                CanKeypad(
                    onDigit = { d -> if (!navigated) state = state.appendDigit(d) },
                    onBackspace = { if (!navigated) state = state.backspace() },
                )
            }
        }
    }
}

@Composable
private fun CanDigitBoxes(
    digits: String,
    masked: Boolean,
) {
    val c = BrandTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        repeat(CAN_LENGTH) { i ->
            val filled = i < digits.length
            val active = i == digits.length && !masked
            Box(
                modifier =
                    Modifier
                        .size(width = 44.dp, height = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (filled) c.violet100 else c.mist)
                        .border(
                            width = 2.dp,
                            color =
                                if (active) {
                                    c.violet
                                } else if (filled) {
                                    c.violet300
                                } else {
                                    c.mist
                                },
                            shape = RoundedCornerShape(12.dp),
                        )
                        .testTag("can_box_$i"),
                contentAlignment = Alignment.Center,
            ) {
                if (filled) {
                    Text(
                        text = if (masked) "•" else digits[i].toString(),
                        color = c.ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CanKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows =
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(' ', '0', '⌫'),
        )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { key ->
                    when (key) {
                        ' ' -> Spacer(Modifier.weight(1f))
                        '⌫' ->
                            KeypadKey(
                                label = "⌫",
                                tag = "can_key_back",
                                filled = false,
                                modifier = Modifier.weight(1f),
                                onClick = onBackspace,
                            )
                        else ->
                            KeypadKey(
                                label = key.toString(),
                                tag = "can_key_$key",
                                filled = true,
                                modifier = Modifier.weight(1f),
                                onClick = { onDigit(key) },
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    tag: String,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val c = BrandTheme.colors
    Box(
        modifier =
            modifier
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (filled) c.white else Color.Transparent)
                .then(
                    if (filled) Modifier.border(1.dp, c.mist, RoundedCornerShape(16.dp)) else Modifier,
                )
                .clickable(onClick = onClick)
                .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = c.ink,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )
    }
}
