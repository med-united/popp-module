package de.servicehealth.poppmodule.demo.insurance

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import de.servicehealth.poppmodule.demo.insurance.apptoapp.UrlDispatcher
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("FunctionName")
fun MainViewController(fqdn: String) =
    ComposeUIViewController {
        val poppSdk =
            remember {
                PoppSdk(context = PoppSdkContext()).also { it.init(fqdn) }
            }

        App(
            poppSdk = poppSdk,
            urlDispatcher = IosUrlDispatcher(),
        )
    }

class IosUrlDispatcher : UrlDispatcher {
    override fun openUrl(url: String) {
        val nsUrl = NSURL(string = url)
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}
