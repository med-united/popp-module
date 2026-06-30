package de.servicehealth.poppmodule.demo.ui.apptoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.demo.thirdparty.auth.OidcParClient
import de.servicehealth.poppmodule.demo.thirdparty.auth.OidcSessionStore
import de.servicehealth.poppmodule.demo.thirdparty.auth.ParResult
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme
import de.servicehealth.poppmodule.theme.PreviewBrandTheme
import kotlinx.coroutines.launch

private const val DEMO_PAR_ENDPOINT = "https://idp.demo.gematik.de/par"
private const val DEMO_AUTH_ENDPOINT = "https://idp.demo.gematik.de/auth"
private const val DEMO_CLIENT_ID = "demo-3rd-party-app"
private const val DEMO_REDIRECT_URI = "myapp://app-to-app/callback"

/** The App-zu-App home screen that initiates the PAR flow. */
@Composable
fun AppToAppHomeScreen(scenarioId: String?) {
    val c = BrandTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var requestUri by remember { mutableStateOf<String?>(null) }
    val parClient = remember { OidcParClient() }

    DisposableEffect(Unit) {
        onDispose {
            parClient.close()
            OidcSessionStore.clear()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(c.mist).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "App-to-App — ${scenarioId ?: "?"}",
                color = c.ink,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            if (requestUri != null) {
                Text(
                    text = "Success! request_uri:\n$requestUri",
                    color = c.violet,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                val uri = requestUri
                Button(onClick = {
                    if (uri != null) {
                        uriHandler.openUri(
                            "$DEMO_AUTH_ENDPOINT?client_id=$DEMO_CLIENT_ID&request_uri=$uri",
                        )
                    }
                }) {
                    Text("Jump to Health Insurance App")
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val result =
                                    parClient.pushAuthorizationRequest(
                                        parEndpoint = DEMO_PAR_ENDPOINT,
                                        clientId = DEMO_CLIENT_ID,
                                        redirectUri = DEMO_REDIRECT_URI,
                                    )
                                when (result) {
                                    is ParResult.Success -> requestUri = result.requestUri
                                    is ParResult.Error -> errorMessage = result.message
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = c.white, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text("Start App-to-App Flow")
                }

                if (errorMessage != null) {
                    Text(
                        text = "Error: $errorMessage",
                        color = c.danger,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview @Composable
private fun AppToAppHomeScreenPreview() {
    CompositionLocalProvider(LocalPoppSdk provides PoppSdk()) {
        PreviewBrandTheme { AppToAppHomeScreen(scenarioId = "scenario-1") }
    }
}
