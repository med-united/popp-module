package de.servicehealth.poppmodule.demo.insurance

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.servicehealth.poppmodule.demo.BrandShowcaseScreen
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.demo.insurance.apptoapp.AppToAppSession
import de.servicehealth.poppmodule.demo.insurance.apptoapp.LocalUrlDispatcher
import de.servicehealth.poppmodule.demo.insurance.apptoapp.UrlDispatcher
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.theme.BrandTheme

@Composable
fun App(
    poppSdk: PoppSdk,
    urlDispatcher: UrlDispatcher,
) {
    BrandTheme {
        CompositionLocalProvider(
            LocalPoppSdk provides poppSdk,
            LocalUrlDispatcher provides urlDispatcher,
        ) {
            val appToAppRequest by AppToAppSession.currentRequest.collectAsState()
            val dispatcher = LocalUrlDispatcher.current

            BrandShowcaseScreen()

            appToAppRequest?.let { request ->
                AlertDialog(
                    onDismissRequest = {
                        val returnUrl = "${request.redirectUri}?error=access_denied&state=${request.state}"
                        AppToAppSession.clearRequest()
                        dispatcher.openUrl(returnUrl)
                    },
                    title = { Text("App-zu-App Check-in Anfrage") },
                    text = { Text("Die Drittanbieter App '${request.clientId}' hat um einen Check-In gebeten.") },
                    confirmButton = {
                        Button(onClick = {
                            val authCode = "mock_auth_code_12345"
                            val returnUrl = "${request.redirectUri}?code=$authCode&state=${request.state}"
                            AppToAppSession.clearRequest()
                            dispatcher.openUrl(returnUrl)
                        }) {
                            Text("Simuliere Erfolg")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            val returnUrl = "${request.redirectUri}?error=access_denied&state=${request.state}"
                            AppToAppSession.clearRequest()
                            dispatcher.openUrl(returnUrl)
                        }) {
                            Text("Ablehnen")
                        }
                    },
                )
            }
        }
    }
}
