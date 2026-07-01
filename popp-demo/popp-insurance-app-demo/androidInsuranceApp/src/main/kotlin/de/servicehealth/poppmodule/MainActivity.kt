package de.servicehealth.poppmodule.demo.insurance

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.insurance.apptoapp.AppToAppSession
import de.servicehealth.poppmodule.demo.insurance.apptoapp.UrlDispatcher
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val poppSdk = PoppSdk(PoppSdkContext(applicationContext))
        poppSdk.init(BuildConfig.POPP_SERVER_FQDN)
        
        handleIntent(intent)

        setContent { 
            App(
                poppSdk = poppSdk, 
                urlDispatcher = AndroidUrlDispatcher(applicationContext)
            ) 
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: android.content.Intent?) {
        intent?.data?.let { uri ->
            AppToAppSession.handleDeepLink(uri.toString())
        }
    }
}

class AndroidUrlDispatcher(private val context: Context) : UrlDispatcher {
    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        poppSdk = PoppSdk(),
        urlDispatcher = object : UrlDispatcher { override fun openUrl(url: String) {} }
    )
}