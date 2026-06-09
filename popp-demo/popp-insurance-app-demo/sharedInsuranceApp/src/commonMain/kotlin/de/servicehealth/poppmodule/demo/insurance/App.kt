package de.servicehealth.poppmodule.demo.insurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import de.servicehealth.poppmodule.demo.BrandShowcaseScreen
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdk

@Composable
fun App(poppSdk: PoppSdk) {
    CompositionLocalProvider(LocalPoppSdk provides poppSdk) {
        BrandShowcaseScreen()
    }
}