package de.servicehealth.poppmodule.demo.insurance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val poppSdk = PoppSdk(PoppSdkContext(applicationContext))
        poppSdk.init(BuildConfig.POPP_SERVER_FQDN)

        setContent { App(poppSdk = poppSdk) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(poppSdk = PoppSdk())
}