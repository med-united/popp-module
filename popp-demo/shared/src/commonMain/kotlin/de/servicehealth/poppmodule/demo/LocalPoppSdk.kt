package de.servicehealth.poppmodule.demo

import androidx.compose.runtime.staticCompositionLocalOf
import de.servicehealth.poppmodule.sdk.PoppSdk

val LocalPoppSdk = staticCompositionLocalOf<PoppSdk> { error("PoppSdk not provided") }
