package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.runtime.Composable

interface AppLauncher {
    val redirectUri: String

    /**
     * Attempts to open the given URL in a native application.
     * Returns true if successfully opened, false if no app was found to handle it.
     */
    suspend fun openUrl(url: String): Boolean
}

@Composable
expect fun rememberAppLauncher(): AppLauncher
