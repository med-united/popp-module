package de.servicehealth.poppmodule.demo

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.servicehealth.poppmodule.demo.generated.resources.Res
import de.servicehealth.poppmodule.demo.generated.resources.insurance_selection_back
import de.servicehealth.poppmodule.demo.generated.resources.insurance_selection_entity_name
import de.servicehealth.poppmodule.demo.generated.resources.insurance_selection_subtitle
import de.servicehealth.poppmodule.demo.generated.resources.insurance_selection_title
import de.servicehealth.poppmodule.sdk.federation.FederationIdp
import de.servicehealth.poppmodule.sdk.federation.FederationMasterClient
import de.servicehealth.poppmodule.theme.BrandCard
import de.servicehealth.poppmodule.theme.BrandCardListPosition
import de.servicehealth.poppmodule.theme.BrandProgressDots
import de.servicehealth.poppmodule.theme.BrandScreenHeader
import de.servicehealth.poppmodule.theme.BrandSearchBody
import de.servicehealth.poppmodule.theme.BrandSearchTexts
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import org.jetbrains.compose.resources.stringResource

// ── Screen ───────────────────────────────────────────────────────────────────────────────────────

@Composable
fun InsuranceSelectionScreen(
    onClose: () -> Unit,
    onBack: () -> Unit = {},
    onInsuranceSelected: (FederationIdp) -> Unit = {},
    applicationTitle: String,
) {
    val c = BrandTheme.colors
    var query by remember { mutableStateOf("") }
    var allInsurances by remember { mutableStateOf<List<FederationIdp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val client = FederationMasterClient(baseUrl = "https://app-ref.federationmaster.de")
            allInsurances = client.fetchIdpList().sortedBy { it.name }
            hasSearched = true
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val results =
        remember(query, allInsurances) {
            if (query.isBlank()) {
                allInsurances
            } else {
                allInsurances.filter { it.name.contains(query, ignoreCase = true) }
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.white)
                .safeContentPadding(),
    ) {
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.insurance_selection_back),
                    tint = c.violet,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable { onBack() },
                )
                Text(
                    text = stringResource(Res.string.insurance_selection_back),
                    color = c.violet,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onBack() },
                )
                Spacer(Modifier.weight(1f))
                BrandProgressDots(stepCount = 4, currentStep = 0)
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.insurance_selection_title),
                color = c.ink,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.insurance_selection_subtitle),
                color = c.ink,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(16.dp))

            BrandSearchBody(
                query = query,
                onQueryChange = { query = it },
                texts = BrandSearchTexts.forEntity(stringResource(Res.string.insurance_selection_entity_name)),
                isLoading = isLoading,
                hasSearched = hasSearched,
                resultsCount = results.size,
                errorMessage = errorMessage,
                emptyContent = null,
                itemsContent = {
                    itemsIndexed(results) { index, insurance ->
                        val listPosition =
                            when {
                                results.size == 1 -> BrandCardListPosition.Standalone
                                index == 0 -> BrandCardListPosition.First
                                index == results.lastIndex -> BrandCardListPosition.Last
                                else -> BrandCardListPosition.Middle
                            }
                        InsuranceRow(
                            insurance = insurance,
                            listPosition = listPosition,
                            onClick = { onInsuranceSelected(insurance) },
                        )
                    }
                },
            )
        }
    }
}

// ── Row item ──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsuranceRow(
    insurance: FederationIdp,
    listPosition: BrandCardListPosition = BrandCardListPosition.Standalone,
    onClick: () -> Unit,
) {
    val c = BrandTheme.colors
    BrandCard(
        onClick = onClick,
        listPosition = listPosition,
        padding = PaddingValues(horizontal = 6.dp, vertical = 13.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(c.violet100),
                contentAlignment = Alignment.Center,
            ) {
                if (insurance.logoUri != null) {
                    AsyncImage(
                        model = insurance.logoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insurance.name,
                    color = c.ink,
                    style = MaterialTheme.typography.titleSmall,
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
}

// ── Previews ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

private val previewInsurances =
    listOf(
        FederationIdp(entityId = "1", name = "Gematiker", logoUri = "https://example.com/logo.png"),
        FederationIdp(entityId = "2", name = "Service-Health Demo KK", logoUri = "https://example.com/logo.png"),
        FederationIdp(entityId = "3", name = "ABC", logoUri = "https://example.com/logo.png"),
    )

@Preview
@Composable
private fun InsuranceSelectionScreen_EmptyPreview() {
    PreviewBrandTheme { InsuranceSelectionScreen(onClose = {}, applicationTitle = "Preview Application") }
}

@Preview
@Composable
private fun InsuranceRow_PharmacyPreview() {
    PreviewBrandTheme { InsuranceRow(insurance = previewInsurances[0], onClick = {}) }
}

@Preview
@Composable
private fun InsuranceRow_PracticePreview() {
    PreviewBrandTheme { InsuranceRow(insurance = previewInsurances[1], onClick = {}) }
}

@Preview
@Composable
private fun InsuranceRow_OnlinePreview() {
    PreviewBrandTheme { InsuranceRow(insurance = previewInsurances[2], onClick = {}) }
}
