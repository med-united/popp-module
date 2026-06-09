package de.servicehealth.poppmodule.demo.insurance

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.sdk.PlatformIdentity
import de.servicehealth.poppmodule.sdk.PoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdkAppConfig
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.PoppSubjectTokenProvider
import de.servicehealth.poppmodule.sdk.TokenProviderConfig

fun MainViewController(fqdn: String) = ComposeUIViewController {
    val poppSdk = remember {
        val appConfig = PoppSdkAppConfig(
            clientName = "popp-demo-insurance-ios",
            platformIdentity = PlatformIdentity.Apple(
                platformType = "ios",
                appBundleIds = listOf("de.servicehealth.poppmodule.demo.insurance"),
            ),
            tokenProvider = TokenProviderConfig.Egk(
                provider = PoppSubjectTokenProvider { error("eGK not yet implemented") }
            ),
        )
        PoppSdk(context = PoppSdkContext(), appConfig = appConfig).also { it.init(fqdn) }
    }
    App(poppSdk = poppSdk)
}