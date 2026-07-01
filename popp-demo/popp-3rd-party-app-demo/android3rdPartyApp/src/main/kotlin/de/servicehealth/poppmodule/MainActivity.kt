package de.servicehealth.poppmodule.demo.thirdparty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.demo.thirdparty.can.createSecureCanStore
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // checkInWithEgk drives the eGK read loop over the direct WebSocket transport at this FQDN
        // (ZETA routing is dormant — see PoppSdk.checkInWithEgk TODO + POPPM-180). The `local` flavor
        // points at ws://localhost:8443/ws; on a phone use `adb reverse tcp:8443 tcp:8443`.
        val poppSdk = PoppSdk(PoppSdkContext(applicationContext))
        poppSdk.init(BuildConfig.POPP_SERVER_FQDN)

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
