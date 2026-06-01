package de.servicehealth.poppmodule.sdk

import kotlin.test.Test
import kotlin.test.assertTrue

class PoppSdkTest {

    @Test
    fun version_isNotBlank() {
        assertTrue(PoppSdk().version().isNotBlank())
    }

    @Test
    fun platformInfo_reportsPlatformName() {
        assertTrue(PoppSdk().platformInfo().contains(getPlatform().name))
    }
}
