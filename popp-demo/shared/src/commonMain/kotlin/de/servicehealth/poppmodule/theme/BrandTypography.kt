package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.servicehealth.poppmodule.demo.generated.resources.*
import org.jetbrains.compose.resources.Font

/**
 * TWK Everett is the display face on the service·health system. We expose two
 * families:
 *  - [displayFamily] for headings, buttons, tags, and other display copy
 *  - [bodyFamily] for body text (system stack — Helvetica Neue / Roboto / Arial)
 */
@Composable
fun displayFamily(): FontFamily = FontFamily(
    Font(Res.font.twk_everett_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.twk_everett_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.twk_everett_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.twk_everett_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.twk_everett_black, FontWeight.Black, FontStyle.Normal),
)

val bodyFamily: FontFamily = FontFamily.Default

@Composable
internal fun brandTypography(): Typography {
    val display = displayFamily()
    val body = bodyFamily
    return Typography(
        displayLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Black,
            fontSize = 48.sp,
            lineHeight = 1.1.em,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 1.15.em,
            letterSpacing = (-0.25).sp
        ),
        displaySmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 1.2.em
        ),
        headlineLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 1.25.em
        ),
        headlineMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 1.3.em
        ),
        headlineSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 1.3.em
        ),
        titleLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 1.35.em
        ),
        titleMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 1.4.em
        ),
        titleSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 1.4.em
        ),
        labelLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 1.2.em,
            letterSpacing = 0.15.sp
        ),
        labelMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 1.2.em,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 1.2.em,
            letterSpacing = 0.7.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 1.45.em
        ),
        bodyMedium = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 1.45.em
        ),
        bodySmall = TextStyle(
            fontFamily = body,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 1.45.em
        ),
    )
}

@Preview
@Composable
private fun BrandTypographyPreview() {
    BrandTheme {
        val t = BrandTheme.typography
        val c = BrandTheme.colors
        Column(
            modifier = Modifier.background(c.white).padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Display Large", style = t.displayLarge, color = c.ink)
            Text("Display Medium", style = t.displayMedium, color = c.ink)
            Text("Display Small", style = t.displaySmall, color = c.ink)
            Text("Headline Large", style = t.headlineLarge, color = c.ink)
            Text("Headline Medium", style = t.headlineMedium, color = c.ink)
            Text("Headline Small", style = t.headlineSmall, color = c.ink)
            Text("Title Large", style = t.titleLarge, color = c.ink)
            Text("Title Medium", style = t.titleMedium, color = c.ink)
            Text("Title Small", style = t.titleSmall, color = c.ink)
            Text("Label Large", style = t.labelLarge, color = c.ink)
            Text("Label Medium", style = t.labelMedium, color = c.ink)
            Text("Label Small", style = t.labelSmall, color = c.ink)
            Text("Body Large", style = t.bodyLarge, color = c.ink)
            Text("Body Medium", style = t.bodyMedium, color = c.ink)
            Text("Body Small", style = t.bodySmall, color = c.ink)
        }
    }
}