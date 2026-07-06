package de.servicehealth.poppmodule.demo.thirdparty.can

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides the active [CanStore] to the check-in screens. Defaults to an in-memory
 * store so Compose previews and tests work without platform wiring; the real
 * encrypted store is provided by `App(canStore = …)`. Mirrors `LocalPoppSdk`.
 */
val LocalCanStore = staticCompositionLocalOf<CanStore> { InMemoryCanStore() }
