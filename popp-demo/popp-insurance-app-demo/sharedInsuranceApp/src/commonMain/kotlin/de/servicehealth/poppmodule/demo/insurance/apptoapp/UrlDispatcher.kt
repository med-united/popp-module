package de.servicehealth.poppmodule.demo.insurance.apptoapp

import androidx.compose.runtime.staticCompositionLocalOf

interface UrlDispatcher {
    fun openUrl(url: String)
}

val LocalUrlDispatcher =
    staticCompositionLocalOf<UrlDispatcher> {
        error("UrlDispatcher not provided")
    }
