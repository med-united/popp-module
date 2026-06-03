package de.servicehealth.poppmodule.demo.ui.launcher

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.theme.BrandTag
import de.servicehealth.poppmodule.theme.BrandTagTone
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import popp_module.popp_demo.shared.generated.resources.Res
import popp_module.popp_demo.shared.generated.resources.launcher_subline
import popp_module.popp_demo.shared.generated.resources.launcher_title
import popp_module.popp_demo.shared.generated.resources.servicehealth_logo

/** The deep navy brand header band: solid tag + white wordmark + title + subline. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrandHeader(
    onWordmarkLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = BrandTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.deep)
            .safeContentPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BrandTag(text = "PoPP-Modul", tone = BrandTagTone.Solid)
            Image(
                painter = painterResource(Res.drawable.servicehealth_logo),
                contentDescription = "service·health",
                colorFilter = ColorFilter.tint(c.white),
                modifier = Modifier
                    .height(26.dp)
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = {}, // tap is a no-op; the wordmark only reacts to long-press (hidden dev/QA showcase)
                        onLongClick = onWordmarkLongPress,
                    ),
            )
            Text(
                text = stringResource(Res.string.launcher_title),
                color = c.white,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(Res.string.launcher_subline),
                color = c.white.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
