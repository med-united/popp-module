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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandField
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.compose.foundation.layout.imePadding

// ── Data model ──────────────────────────────────────────────────────────────

enum class InstitutionType { PHARMACY, PRACTICE, ONLINE }

data class Institution(
    val id: String,
    val name: String,
    val address: String,
    val type: InstitutionType,
    val telematicsId: String,
)

// ── FHIR-VZD search ─────────────────────────────────────────────────────────

@Serializable
private data class FhirBundle(
    val entry: List<FhirEntry>? = null,
    val total: Int? = null,
)

@Serializable
private data class FhirEntry(
    val resource: FhirResource? = null,
)

@Serializable
private data class FhirResource(
    val id: String? = null,
    val name: List<FhirName>? = null,
    val address: List<FhirAddress>? = null,
    val identifier: List<FhirIdentifier>? = null,
    val type: List<FhirType>? = null,
)

@Serializable
private data class FhirName(val text: String? = null)

@Serializable
private data class FhirAddress(
    val line: List<String>? = null,
    val city: String? = null,
    val postalCode: String? = null,
)

@Serializable
private data class FhirIdentifier(
    val system: String? = null,
    val value: String? = null,
)

@Serializable
private data class FhirType(
    val coding: List<FhirCoding>? = null,
)

@Serializable
private data class FhirCoding(
    val code: String? = null,
)

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

private suspend fun searchInstitutions(query: String): List<Institution> {
    if (query.isBlank()) return emptyList()
    return try {
        val bundle: FhirBundle = httpClient.get(
            "https://fhir-directory-tu.app.ti-dienste.de/tim-provider-services"
        ) {
            parameter("name", query)
            parameter("_count", 20)
        }.body()

        bundle.entry.orEmpty().mapNotNull { entry ->
            val res = entry.resource ?: return@mapNotNull null
            val name = res.name?.firstOrNull()?.text ?: return@mapNotNull null
            val addr = res.address?.firstOrNull()
            val line = addr?.line?.firstOrNull() ?: ""
            val city = listOfNotNull(addr?.postalCode, addr?.city).joinToString(" ")
            val address = listOf(line, city).filter { it.isNotBlank() }.joinToString(", ")
            val telematicsId = res.identifier
                ?.firstOrNull { it.system?.contains("telematik") == true }
                ?.value ?: res.id ?: ""
            val typeCode = res.type?.firstOrNull()?.coding?.firstOrNull()?.code ?: ""
            val type = when {
                typeCode.contains("APO", ignoreCase = true) || name.contains("Apotheke", ignoreCase = true) -> InstitutionType.PHARMACY
                typeCode.contains("online", ignoreCase = true) || address.contains("bundesweit", ignoreCase = true) -> InstitutionType.ONLINE
                else -> InstitutionType.PRACTICE
            }
            Institution(
                id = res.id ?: "",
                name = name,
                address = address.ifBlank { "Adresse nicht verfügbar" },
                type = type,
                telematicsId = telematicsId,
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun InstitutionSearchScreen(
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
        results = searchInstitutions(query)
        isLoading = false
        hasSearched = true
    }

    BrandTheme {
        Column(
            modifier = Modifier
                .background(c.mist)
                .fillMaxSize()
        ) {
            // ── Top bar ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.white)
                    .safeContentPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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
                    // Step indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(width = 20.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(c.violet))
                        Box(Modifier.size(width = 12.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(c.silver))
                        Box(Modifier.size(width = 12.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(c.silver))
                    }
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

                // Search field
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

                // ── States ───────────────────────────────────────────────
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            BrandSpinner(color = c.violet)
                        }
                    }

                    query.isBlank() -> {
                        // Empty state
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
                        // Result count
                        Text(
                            text = "${results.size} Ergebnis${if (results.size != 1) "se" else ""}",
                            color = c.neutral700,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(10.dp))

                        // Results list
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
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(c.violet100),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (institution.type) {
                        InstitutionType.PHARMACY -> "\uD83C\uDFE0"  // 🏠 placeholder, replace with real icon
                        InstitutionType.PRACTICE -> "\uD83E\uDE7A"  // 🩺
                        InstitutionType.ONLINE -> "\uD83D\uDCF9"    // 📹
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