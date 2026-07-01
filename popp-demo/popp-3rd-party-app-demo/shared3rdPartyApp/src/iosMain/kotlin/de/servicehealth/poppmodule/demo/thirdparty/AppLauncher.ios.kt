package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

@Composable
actual fun rememberAppLauncher(): AppLauncher {
    return remember {
        object : AppLauncher {
            override val redirectUri = "https://popp.service-health.de/callback"

            override suspend fun openUrl(url: String): Boolean =
                suspendCancellableCoroutine { cont ->
                    val nsUrl = NSURL(string = url)
                    if (nsUrl == null) {
                        cont.resume(false)
                        return@suspendCancellableCoroutine
                    }

                    UIApplication.sharedApplication.openURL(
                        url = nsUrl,
                        options = emptyMap<Any?, Any?>(),
                        completionHandler = { success ->
                            cont.resume(success)
                        },
                    )
                }
        }
    }
}
