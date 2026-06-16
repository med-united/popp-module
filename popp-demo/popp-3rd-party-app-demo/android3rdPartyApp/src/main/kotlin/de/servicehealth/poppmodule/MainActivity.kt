package de.servicehealth.poppmodule.demo.thirdparty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.demo.thirdparty.can.createSecureCanStore
import de.servicehealth.poppmodule.sdk.PoppDevTransport
import de.servicehealth.poppmodule.sdk.PoppSdk

@OptIn(PoppDevTransport::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // DEV: local-stack direct transport (no ZETA). Flavor-selected; default `local`.
        // On a phone use `adb reverse tcp:8443 tcp:8443` so localhost:8443 reaches the host stack.
        val poppSdk = PoppSdk.directTransport(BuildConfig.POPP_SERVER_FQDN)

        setContent {
            App(poppSdk = poppSdk, canStore = createSecureCanStore(applicationContext))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
