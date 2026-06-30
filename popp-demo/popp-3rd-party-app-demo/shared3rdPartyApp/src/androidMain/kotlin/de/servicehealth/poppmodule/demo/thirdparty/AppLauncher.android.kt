package de.servicehealth.poppmodule.demo.thirdparty

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppLauncher(): AppLauncher {
    val context = LocalContext.current
    return remember(context) {
        object : AppLauncher {
            override suspend fun openUrl(url: String): Boolean {
                return try {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
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
