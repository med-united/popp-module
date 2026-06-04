package de.servicehealth.poppmodule.demo.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.model.IntegrationMode
import de.servicehealth.poppmodule.demo.model.demoScenarios
import de.servicehealth.poppmodule.theme.BrandButton
import de.servicehealth.poppmodule.theme.BrandButtonSize
import de.servicehealth.poppmodule.theme.BrandButtonVariant
import de.servicehealth.poppmodule.theme.BrandSegmented
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.SegmentedOption
import org.jetbrains.compose.resources.stringResource
import de.servicehealth.poppmodule.demo.generated.resources.Res
import de.servicehealth.poppmodule.demo.generated.resources.mode_app2app_desc
import de.servicehealth.poppmodule.demo.generated.resources.mode_app2app_title
import de.servicehealth.poppmodule.demo.generated.resources.mode_integrated_desc
import de.servicehealth.poppmodule.demo.generated.resources.mode_integrated_title
import de.servicehealth.poppmodule.demo.generated.resources.start_demo_button

/** Enum round-trips through its `name` so the segmented selection survives recreation. */
val IntegrationModeSaver: Saver<IntegrationMode, String> = Saver(
    save = { it.name },
    restore = { IntegrationMode.valueOf(it) },
)

/**
 * The launcher start screen. Selects one scenario (single-select; none initially) and
 * one integration mode (always set; defaults to INTEGRATED). Start is enabled once a
 * scenario is chosen, and reports `(scenarioId, mode)` to the caller.
 */
@Composable
fun PoppLauncherScreen(
    onStartDemo: (scenarioId: String, mode: IntegrationMode) -> Unit,
    onOpenShowcase: () -> Unit,
) {
    var selectedScenario by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMode by rememberSaveable(stateSaver = IntegrationModeSaver) {
        mutableStateOf(IntegrationMode.INTEGRATED)
    }
    val c = BrandTheme.colors

    Scaffold(
        topBar = { BrandHeader(onWordmarkLongPress = onOpenShowcase) },
        containerColor = c.mist,
        bottomBar = {
            BrandButton(
                text = stringResource(Res.string.start_demo_button),
                onClick = {
                    val scenario = selectedScenario
                    if (scenario != null) onStartDemo(scenario, selectedMode)
                },
                enabled = selectedScenario != null,
                variant = BrandButtonVariant.Primary,
                size = BrandButtonSize.Lg,
                modifier = Modifier.fillMaxWidth().safeContentPadding().padding(horizontal = 20.dp),
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // --- Scenario section (AC2) ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Szenario".uppercase(),
                        color = c.violet,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    demoScenarios.forEach { scenario ->
                        ScenarioCard(
                            title = stringResource(scenario.title),
                            subtitle = stringResource(scenario.subtitle),
                            selected = scenario.id == selectedScenario,
                            onClick = { selectedScenario = scenario.id },
                        )
                    }
                }
                // --- Integration mode section (AC3) ---
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Integration".uppercase(),
                        color = c.violet,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    BrandSegmented(
                        options = listOf(
                            SegmentedOption(IntegrationMode.INTEGRATED, stringResource(Res.string.mode_integrated_title)),
                            SegmentedOption(IntegrationMode.APP_TO_APP, stringResource(Res.string.mode_app2app_title)),
                        ),
                        selected = selectedMode,
                        onSelect = { selectedMode = it },
                    )
                    Text(
                        text = stringResource(
                            if (selectedMode == IntegrationMode.INTEGRATED) Res.string.mode_integrated_desc
                            else Res.string.mode_app2app_desc
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.neutral700,
                    )
                }
            }
        }
    }
}
