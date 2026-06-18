package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkContext

@Suppress("FunctionName")
fun MainViewController(fqdn: String) =
    ComposeUIViewController {
        val poppSdk =
            remember {
                PoppSdk(context = PoppSdkContext()).also { it.init(fqdn) }
            }

        App(poppSdk = poppSdk)
    }
