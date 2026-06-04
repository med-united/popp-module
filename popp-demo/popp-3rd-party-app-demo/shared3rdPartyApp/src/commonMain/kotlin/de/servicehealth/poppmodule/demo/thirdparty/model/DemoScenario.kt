package de.servicehealth.poppmodule.demo.model

import de.servicehealth.poppmodule.demo.generated.resources.Res
import org.jetbrains.compose.resources.StringResource
import de.servicehealth.poppmodule.demo.generated.resources.scenario_pharmacy_subtitle
import de.servicehealth.poppmodule.demo.generated.resources.scenario_pharmacy_title
import de.servicehealth.poppmodule.demo.generated.resources.scenario_telemedicine_subtitle
import de.servicehealth.poppmodule.demo.generated.resources.scenario_telemedicine_title
import de.servicehealth.poppmodule.demo.generated.resources.scenario_therapy_subtitle
import de.servicehealth.poppmodule.demo.generated.resources.scenario_therapy_title

/** A healthcare use case shown on the launcher. `id` is the stable nav argument value. */
data class DemoScenario(
    val id: String,
    val title: StringResource,
    val subtitle: StringResource,
)

/** The fixed set of scenarios offered on the launcher (AC2). Add/remove = list edit. */
val demoScenarios = listOf(
    DemoScenario("online_pharmacy", Res.string.scenario_pharmacy_title, Res.string.scenario_pharmacy_subtitle),
    DemoScenario("telemedicine", Res.string.scenario_telemedicine_title, Res.string.scenario_telemedicine_subtitle),
    DemoScenario("therapy", Res.string.scenario_therapy_title, Res.string.scenario_therapy_subtitle),
)
