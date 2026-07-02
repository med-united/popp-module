package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_back
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_clear
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_empty_hint
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_field_placeholder
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_no_results
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.institution_search_title
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.search_result_count
import de.servicehealth.poppmodule.theme.BrandBackButton
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandField
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandSpinner
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

enum class InstitutionType { PHARMACY, PRACTICE, ONLINE }

data class Institution(
    val id: String,
    val name: String,
    val address: String,
    val type: InstitutionType,
    val telematicsId: String,
)

@Composable
fun InstitutionSearchScreen(
    onClose: () -> Unit,
    onBack: () -> Unit = {},
    onInstitutionSelected: (Institution) -> Unit = {},
    favoriteIds: Set<String> = emptySet(),
    applicationTitle: String,
) {
    val c = BrandTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Institution>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
        results =
            mockInstitutions.filter { institution ->
                institution.name.contains(query, ignoreCase = true) ||
                    institution.address.contains(query, ignoreCase = true)
            }
        isLoading = false
        hasSearched = true
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
    ) {
        // ── Header -──────────────────────────────────────────────────
        BrandScreenHeader(title = applicationTitle, onClose = onClose)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BrandBackButton(label = stringResource(Res.string.institution_search_back), onClick = onBack)
                Spacer(Modifier.weight(1f))
                BrandProgressDots(stepCount = 4, currentStep = 0)
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 12.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.institution_search_title),
                color = c.ink,
                style = BrandTheme.typography.displaySmall,
            )

            Spacer(Modifier.height(16.dp))

            BrandField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.institution_search_field_placeholder),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (query.isNotEmpty()) c.violet else c.silver,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon =
                    if (query.isNotEmpty()) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.institution_search_clear),
                                tint = c.silver,
                                modifier =
                                    Modifier
                                        .size(18.dp)
                                        .clickable { query = "" },
                            )
                        }
                    } else {
                        null
                    },
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
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = stringResource(Res.string.institution_search_empty_hint),
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
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = stringResource(Res.string.institution_search_no_results),
                            color = c.neutral700,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                results.isNotEmpty() -> {
                    Text(
                        text = pluralStringResource(Res.plurals.search_result_count, results.size, results.size),
                        color = c.neutral700,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyColumn {
                        item {
                            BrandCard(padding = PaddingValues(0.dp)) {
                                Column {
                                    results.forEachIndexed { index, institution ->
                                        InstitutionRow(
                                            institution = institution,
                                            isFavorite = institution.id in favoriteIds,
                                            onClick = { onInstitutionSelected(institution) },
                                        )
                                        if (index != results.lastIndex) {
                                            HorizontalDivider(color = c.mist)
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstitutionRow(
    institution: Institution,
    onClick: () -> Unit,
    isFavorite: Boolean = false,
) {
    val c = BrandTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(c.violet100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = institution.type.icon(),
                contentDescription = null,
                tint = c.violet,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = institution.name,
                    color = c.ink,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (isFavorite) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = c.yellow,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
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
            modifier = Modifier.size(20.dp),
        )
    }
}

val mockInstitutions =
    listOf(
        Institution(
            id = "1",
            name = "Apotheke am Markt",
            address = "Marktplatz 3, 52062 Aachen",
            type = InstitutionType.PHARMACY,
            telematicsId = "3-SMC-B-Testkarte-883110000117894",
        ),
        Institution(
            id = "2",
            name = "Hausarztpraxis Dr. Brandt",
            address = "Theaterstraße 18, 52062 Aachen",
            type = InstitutionType.PRACTICE,
            telematicsId = "3-SMC-B-Testkarte-883110000229865",
        ),
    )

val InstitutionType.label: String
    get() =
        when (this) {
            InstitutionType.PHARMACY -> "Apotheke"
            InstitutionType.PRACTICE -> "Hausarztpraxis"
            InstitutionType.ONLINE -> "Online"
        }

fun InstitutionType.icon(): ImageVector =
    when (this) {
        InstitutionType.PHARMACY -> Icons.Outlined.LocalPharmacy
        InstitutionType.PRACTICE -> Icons.Outlined.MedicalServices
        InstitutionType.ONLINE -> Icons.Outlined.Videocam
    }

@Preview
@Composable
private fun InstitutionSearchScreen_EmptyPreview() {
    PreviewBrandTheme { InstitutionSearchScreen(onClose = {}, applicationTitle = "Preview Application") }
}

@Preview
@Composable
private fun InstitutionSearchScreen_ResultsPreview() {
    val c = BrandTheme.colors
    BrandTheme {
        Column(
            modifier =
                Modifier
                    .background(c.mist)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${mockInstitutions.size} Ergebnisse",
                color = c.neutral700,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(2.dp))
            BrandCard(padding = PaddingValues(0.dp)) {
                Column {
                    mockInstitutions.forEachIndexed { index, institution ->
                        InstitutionRow(institution = institution, isFavorite = index == 0, onClick = {})
                        if (index != mockInstitutions.lastIndex) {
                            HorizontalDivider(color = c.mist)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InstitutionRow_PharmacyPreview() {
    BrandTheme {
        BrandCard(padding = PaddingValues(0.dp)) {
            InstitutionRow(institution = mockInstitutions[0], isFavorite = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun InstitutionRow_PracticePreview() {
    BrandTheme {
        BrandCard(padding = PaddingValues(0.dp)) {
            InstitutionRow(institution = mockInstitutions[1], onClick = {})
        }
    }
}
