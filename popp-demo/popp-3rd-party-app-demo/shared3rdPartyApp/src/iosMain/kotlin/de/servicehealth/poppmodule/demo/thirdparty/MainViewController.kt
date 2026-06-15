package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.ui.window.ComposeUIViewController
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.demo.thirdparty.can.createSecureCanStore

@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController { App(canStore = createSecureCanStore()) }
