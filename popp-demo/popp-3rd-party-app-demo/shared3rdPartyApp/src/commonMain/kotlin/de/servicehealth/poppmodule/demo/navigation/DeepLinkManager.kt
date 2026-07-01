package de.servicehealth.poppmodule.demo.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkManager {
    private val _deepLinks = MutableSharedFlow<String>(replay = 1)
    val deepLinks = _deepLinks.asSharedFlow()

    fun handleDeepLink(url: String) {
        _deepLinks.tryEmit(url)
    }

    fun clearReplay() {
        _deepLinks.resetReplayCache()
    }
}
