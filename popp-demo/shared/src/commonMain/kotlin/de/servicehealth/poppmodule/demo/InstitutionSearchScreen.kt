package de.servicehealth.poppmodule.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandField
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import kotlinx.coroutines.delay

// ── Data model ──────────────────────────────────────────────────────────────

enum class InstitutionType { PHARMACY, PRACTICE, ONLINE }

data class Institution(
    val id: String,
    val name: String,
    val address: String,
    val type: InstitutionType,
    val telematicsId: String,
)

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun InstitutionSearchScreen(
    onClose: () -> Unit,
    onBack: () -> Unit = {},
    onInstitutionSelected: (Institution) -> Unit = {},
) {
    val c = BrandTheme.colors
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Institution>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Debounced search — fires 400ms after user stops typing
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            hasSearched = false
            isLoading = false
            return@LaunchedEffect
        }
        delay(400)
        isLoading = true
        errorMessage = null
        results = emptyList() // TODO: call PoppSdk.searchInstitutions(query) — POPPM-116
        isLoading = false
        hasSearched = true
    }

    BrandTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding()
        ) {

            // ── Header -──────────────────────────────────────────────────
            BrandScreenHeader(title = "VOR-ORT-CHECK-IN", onClose = onClose)

            // ── Navigation ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = c.violet,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBack() }
                    )
                    Text(
                        text = "Zurück",
                        color = c.violet,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { onBack() }
                    )
                    Spacer(Modifier.weight(1f))
                    BrandProgressDots(stepCount = 4, currentStep = 1)
                }
            }

            // ── Content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Einrichtung suchen",
                    color = c.ink,
                    style = MaterialTheme.typography.displaySmall,
                )

                Spacer(Modifier.height(16.dp))

                BrandField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Apotheke oder Praxis suchen\u2026",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (query.isNotEmpty()) c.violet else c.silver,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Löschen",
                                tint = c.silver,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { query = "" }
                            )
                        }
                    } else null,
                )

                Spacer(Modifier.height(20.dp))

                when {
                    isLoading -> {
                        Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            BrandSpinner(color = c.violet)
                        }
                    }

                    query.isBlank() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = c.silver,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Tippen Sie z.\u00a0B. \u201eApotheke\u201c, \u201eMarkt\u201c\noder einen Praxisnamen, um\nEinrichtungen zu finden.",
                                color = c.neutral700,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }

                    hasSearched && results.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = c.silver,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Keine Einrichtung gefunden.",
                                color = c.neutral700,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    results.isNotEmpty() -> {
                        Text(
                            text = "${results.size} Ergebnis${if (results.size != 1) "se" else ""}",
                            color = c.neutral700,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(results) { institution ->
                                InstitutionRow(
                                    institution = institution,
                                    onClick = { onInstitutionSelected(institution) }
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Row item ─────────────────────────────────────────────────────────────────

@Composable
private fun InstitutionRow(
    institution: Institution,
    onClick: () -> Unit,
) {
    val c = BrandTheme.colors
    BrandCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(c.violet100),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (institution.type) {
                        InstitutionType.PHARMACY -> "\uD83C\uDFE0"
                        InstitutionType.PRACTICE -> "\uD83E\uDE7A"
                        InstitutionType.ONLINE -> "\uD83D\uDCF9"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = institution.name,
                    color = c.ink,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = institution.address,
                    color = c.neutral700,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = c.silver,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewInstitutions = listOf(
    Institution(
        id = "1",
        name = "Apotheke am Markt",
        address = "Marktplatz 1, 10117 Berlin",
        type = InstitutionType.PHARMACY,
        telematicsId = "3-SMC-B-Testkarte-883110000117894",
    ),
    Institution(
        id = "2",
        name = "Hausarztpraxis Dr. Müller",
        address = "Hauptstraße 42, 80331 München",
        type = InstitutionType.PRACTICE,
        telematicsId = "3-SMC-B-Testkarte-883110000229865",
    ),
    Institution(
        id = "3",
        name = "Online-Arzt Digital GmbH",
        address = "digital",
        type = InstitutionType.ONLINE,
        telematicsId = "3-SMC-B-Testkarte-883110000334521",
    ),
)

@Preview
@Composable
private fun InstitutionSearchScreen_EmptyPreview() {
    InstitutionSearchScreen(onClose = {})
}

@Preview
@Composable
private fun InstitutionSearchScreen_ResultsPreview() {
    val c = BrandTheme.colors
    BrandTheme {
        Column(
            modifier = Modifier
                .background(c.mist)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${previewInstitutions.size} Ergebnisse",
                color = c.neutral700,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(2.dp))
            previewInstitutions.forEach { institution ->
                InstitutionRow(institution = institution, onClick = {})
            }
        }
    }
}

@Preview
@Composable
private fun InstitutionRow_PharmacyPreview() {
    BrandTheme {
        InstitutionRow(institution = previewInstitutions[0], onClick = {})
    }
}

@Preview
@Composable
private fun InstitutionRow_PracticePreview() {
    BrandTheme {
        InstitutionRow(institution = previewInstitutions[1], onClick = {})
    }
}

@Preview
@Composable
private fun InstitutionRow_OnlinePreview() {
    BrandTheme {
        InstitutionRow(institution = previewInstitutions[2], onClick = {})
    }
}