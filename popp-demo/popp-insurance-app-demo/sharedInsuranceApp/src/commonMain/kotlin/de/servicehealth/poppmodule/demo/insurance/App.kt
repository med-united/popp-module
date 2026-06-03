package de.servicehealth.poppmodule.demo.insurance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.BrandShowcaseScreen
import de.servicehealth.poppmodule.demo.LocalPoppSdk
import de.servicehealth.poppmodule.sdk.PoppSdk

@Composable
@Preview
fun App() {
    CompositionLocalProvider(LocalPoppSdk provides PoppSdk()) {
        var showCheckIn by remember { mutableStateOf(false) }
        if (showCheckIn) {
            OnsiteCheckInEntryScreen(
                onClose = { showCheckIn = false },
                onSearchClick = {},
                onQrScanClick = {},
            )
        } else {
            BrandShowcaseScreen(onOpenCheckIn = { showCheckIn = true })
        }
    }
}
