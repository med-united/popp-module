package de.servicehealth.poppmodule.demo.thirdparty

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.demo.navigation.DeepLinkManager
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.toString()?.let { DeepLinkManager.handleDeepLink(it) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(poppSdk = PoppSdk())
}