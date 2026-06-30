package de.servicehealth.poppmodule.demo.thirdparty

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppLauncher(): AppLauncher {
    val context = LocalContext.current
    return remember(context) {
        object : AppLauncher {
            override val redirectUri = "https://demo.popp.de/callback"

            override suspend fun openUrl(url: String): Boolean {
                return try {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
                                } else {
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                        }
                    context.startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    false
                }
            }
        }
    }
}
