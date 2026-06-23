package de.servicehealth.poppmodule.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandColors
import de.servicehealth.poppmodule.theme.BrandField
import de.servicehealth.poppmodule.theme.BrandSegmented
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTag
import de.servicehealth.poppmodule.theme.BrandTagTone
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import de.servicehealth.poppmodule.theme.SegmentedOption

/**
 * Full visual showcase of the service·health brand primitives. Renders one
 * "story" section per component so the app launches into a single scrollable
 * page that demonstrates the theme end-to-end.
 */
@Composable
fun BrandShowcaseScreen(onOpenCheckIn: (() -> Unit)? = null) {
    val c = BrandTheme.colors
    Column(
        modifier =
            Modifier
                .background(c.mist)
                .fillMaxSize(),
    ) {
        ShowcaseHeader()
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeContentPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            if (onOpenCheckIn != null) {
                BrandButton(
                    text = "Vor-Ort-Check-In öffnen",
                    onClick = onOpenCheckIn,
                    variant = BrandButtonVariant.Primary,
                    size = BrandButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SdkSection()
            ColorSection(c)
            TypographySection()
            ButtonSection()
            TagSection()
            CardSection()
            FieldSection()
            SegmentedSection()
            SpinnerSection()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShowcaseHeader() {
    val c = BrandTheme.colors
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(c.deep)
                .safeContentPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BrandTag(text = "PoPP-Modul", tone = BrandTagTone.Solid)
            Text(
                text = "service·health",
                color = c.white,
                style = BrandTheme.typography.displaySmall,
            )
            Text(
                text = "Theme- und Komponenten-Showcase",
                color = c.white.copy(alpha = 0.7f),
                style = BrandTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SectionHeading(
    eyebrow: String,
    title: String,
) {
    val c = BrandTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow.uppercase(),
            color = c.violet,
            style = BrandTheme.typography.labelSmall,
        )
        Text(
            text = title,
            color = c.ink,
            style = BrandTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun SdkSection() {
    val c = BrandTheme.colors
    val sdk = LocalPoppSdk.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "00 · SDK", title = "PoPP-SDK Integration")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = sdk.version(),
                    color = c.ink,
                    style = BrandTheme.typography.titleMedium,
                )
                Text(
                    text = "Running on ${sdk.platformInfo()}",
                    color = c.ink.copy(alpha = 0.7f),
                    style = BrandTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ColorSection(c: BrandColors) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "01 · Farben", title = "Palette")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ColorRow(
                    "Electric Violet",
                    listOf(
                        "violet" to c.violet,
                        "violet700" to c.violet700,
                        "violet300" to c.violet300,
                        "violet100" to c.violet100,
                    ),
                )
                ColorRow(
                    "Signal Yellow",
                    listOf(
                        "yellow" to c.yellow,
                        "yellow300" to c.yellow300,
                        "yellow100" to c.yellow100,
                    ),
                )
                ColorRow(
                    "Deep & Ink",
                    listOf(
                        "deep" to c.deep,
                        "ink" to c.ink,
                        "neutral700" to c.neutral700,
                        "silver" to c.silver,
                    ),
                )
                ColorRow(
                    "Surface",
                    listOf(
                        "mist" to c.mist,
                        "white" to c.white,
                    ),
                )
                ColorRow(
                    "Semantik",
                    listOf(
                        "success" to c.success,
                        "warning" to c.warning,
                        "danger" to c.danger,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ColorRow(
    label: String,
    swatches: List<Pair<String, Color>>,
) {
    val c = BrandTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = c.neutral700,
            style = BrandTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            swatches.forEach { (name, color) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color),
                    )
                    Text(
                        text = name,
                        color = c.neutral700,
                        style = BrandTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun TypographySection() {
    val c = BrandTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "02 · Typografie", title = "TWK Everett · Helvetica")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Display Large", color = c.ink, style = BrandTheme.typography.displayLarge)
                Text("Display Medium", color = c.ink, style = BrandTheme.typography.displayMedium)
                Text("Headline Medium", color = c.ink, style = BrandTheme.typography.headlineMedium)
                Text("Title Large · eGK bereit", color = c.ink, style = BrandTheme.typography.titleLarge)
                Text(
                    text = "Body Large — die Versichertendaten werden sicher per NFC übertragen.",
                    color = c.neutral700,
                    style = BrandTheme.typography.bodyLarge,
                )
                Text(
                    text = "Body Medium · 14 sp",
                    color = c.neutral700,
                    style = BrandTheme.typography.bodyMedium,
                )
                Text(
                    text = "Label Small · 10 sp · uppercase".uppercase(),
                    color = c.violet,
                    style = BrandTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ButtonSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "03 · Buttons", title = "Varianten & Größen")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandButton(
                    text = "eGK scannen",
                    onClick = {},
                    variant = BrandButtonVariant.Primary,
                    size = BrandButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandButton(
                    text = "Abbrechen",
                    onClick = {},
                    variant = BrandButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BrandButton(text = "Ghost", onClick = {}, variant = BrandButtonVariant.Ghost)
                    BrandButton(text = "Akzent", onClick = {}, variant = BrandButtonVariant.Accent)
                    BrandButton(text = "Dark", onClick = {}, variant = BrandButtonVariant.Dark)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandButton(text = "Klein", onClick = {}, size = BrandButtonSize.Sm)
                    BrandButton(text = "Standard", onClick = {}, size = BrandButtonSize.Md)
                    BrandButton(text = "Groß", onClick = {}, size = BrandButtonSize.Lg)
                }
                BrandButton(
                    text = "Deaktiviert",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TagSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "04 · Tags", title = "Status & Labels")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRowTwoLines {
                    BrandTag(text = "Violet", tone = BrandTagTone.Violet)
                    BrandTag(text = "Solid", tone = BrandTagTone.Solid)
                    BrandTag(text = "Yellow", tone = BrandTagTone.Yellow)
                    BrandTag(text = "Neutral", tone = BrandTagTone.Neutral)
                }
                FlowRowTwoLines {
                    BrandTag(text = "Erfolg", tone = BrandTagTone.Success, dot = true)
                    BrandTag(text = "Hinweis", tone = BrandTagTone.Warning, dot = true)
                    BrandTag(text = "Live", tone = BrandTagTone.Solid, dot = true)
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandTheme.colors.deep)
                            .padding(14.dp),
                ) {
                    BrandTag(text = "On Dark", tone = BrandTagTone.OnDark, dot = true)
                }
            }
        }
    }
}

@Composable
private fun FlowRowTwoLines(content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

@Composable
private fun CardSection() {
    val c = BrandTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "05 · Cards", title = "Standard & Raised")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Adler-Apotheke", color = c.ink, style = BrandTheme.typography.titleMedium)
                Text("Adalbertsteinweg 12, 52070 Aachen", color = c.neutral700, style = BrandTheme.typography.bodyMedium)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandTag(text = "Öffnet 08:00", tone = BrandTagTone.Success, dot = true)
                    BrandTag(text = "Favorit", tone = BrandTagTone.Violet)
                }
            }
        }
        BrandCard(
            raised = true,
            padding = PaddingValues(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(c.violet100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("LH", color = c.violet700, style = BrandTheme.typography.titleMedium)
                }
                Column {
                    Text("Lena Hofmann", color = c.ink, style = BrandTheme.typography.titleLarge)
                    Text("eGK · A123 456 789", color = c.neutral700, style = BrandTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun FieldSection() {
    val c = BrandTheme.colors
    var search by remember { mutableStateOf("") }
    var can by remember { mutableStateOf("493817") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "06 · Inputs", title = "Field")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("LEI suchen", color = c.ink, style = BrandTheme.typography.titleSmall)
                BrandField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Apotheke oder Praxis suchen…",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { focused ->
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (focused) c.violet else c.silver),
                        )
                    },
                )
                Text("CAN", color = c.ink, style = BrandTheme.typography.titleSmall)
                BrandField(
                    value = can,
                    onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) can = it },
                    placeholder = "6-stellige CAN",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SegmentedSection() {
    val c = BrandTheme.colors
    var scenario by remember { mutableStateOf("integrated") }
    var platform by remember { mutableStateOf("both") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "07 · Segmented", title = "Light & Dark")
        BrandCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Integrationsszenario", color = c.ink, style = BrandTheme.typography.titleSmall)
                BrandSegmented(
                    options =
                        listOf(
                            SegmentedOption("integrated", "Voll integriert"),
                            SegmentedOption("app2app", "App-zu-App"),
                        ),
                    selected = scenario,
                    onSelect = { scenario = it },
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(c.deep)
                            .padding(14.dp),
                ) {
                    BrandSegmented(
                        options =
                            listOf(
                                SegmentedOption("both", "Beide"),
                                SegmentedOption("ios", "iOS"),
                                SegmentedOption("android", "Android"),
                            ),
                        selected = platform,
                        onSelect = { platform = it },
                        dark = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpinnerSection() {
    val c = BrandTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(eyebrow = "08 · Spinner", title = "Progress")
        BrandCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                BrandSpinner(color = c.violet)
                BrandSpinner(color = c.deep, size = 28.dp)
                BrandSpinner(color = c.yellow, size = 36.dp, strokeWidth = 4.dp)
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("eGK wird gelesen…", color = c.ink, style = BrandTheme.typography.titleSmall)
                    Text("NFC aktiv", color = c.success, style = BrandTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Preview @Composable
private fun BrandShowcaseScreenPreview() {
    CompositionLocalProvider(LocalPoppSdk provides PoppSdk()) {
        PreviewBrandTheme { BrandShowcaseScreen() }
    }
}
