package de.servicehealth.poppmodule.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class BrandSearchTexts(
    val placeholder: String,
    val noResultsText: String,
) {
    companion object {
        fun forEntity(name: String) =
            BrandSearchTexts(
                placeholder = "$name suchen…",
                noResultsText = "Keine $name gefunden.",
            )
    }
}

@Composable
fun BrandSearchBody(
    query: String,
    onQueryChange: (String) -> Unit,
    texts: BrandSearchTexts,
    isLoading: Boolean,
    hasSearched: Boolean,
    resultsCount: Int,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    emptyContent: @Composable (() -> Unit)? = null,
    itemsContent: LazyListScope.() -> Unit,
) {
    val c = BrandTheme.colors
    Column(modifier = modifier) {
        BrandField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = texts.placeholder,
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
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Löschen",
                                tint = c.silver,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                } else {
                    null
                },
        )

        Spacer(Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BrandSpinner(color = c.violet)
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = c.danger,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            resultsCount > 0 -> {
                Text(
                    text = "$resultsCount Ergebnis${if (resultsCount != 1) "se" else ""}",
                    color = c.neutral700,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn {
                    itemsContent()
                }
            }

            hasSearched -> {
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
                        text = texts.noResultsText,
                        color = c.neutral700,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            else -> emptyContent?.invoke()
        }
    }
}
