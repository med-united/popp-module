package de.servicehealth.poppmodule.sdk.qr

import kotlin.test.Test
import kotlin.test.assertFalse

class NoCameraDependencyTest {
    @Test
    fun camerax_is_not_on_the_popp_sdk_classpath() {
        assertFalse(
            classExists("androidx.camera.core.SurfaceRequest"),
            "CameraX must not be a dependency of :popp-sdk (see ADR-002) — it belongs in :popp-sdk-qr",
        )
    }

    @Test
    fun mlkit_barcode_is_not_on_the_popp_sdk_classpath() {
        assertFalse(
            classExists("com.google.mlkit.vision.barcode.BarcodeScanning"),
            "ML Kit barcode-scanning must not be a dependency of :popp-sdk (see ADR-002) — it belongs in :popp-sdk-qr",
        )
    }

    private fun classExists(fqcn: String): Boolean = runCatching { Class.forName(fqcn) }.isSuccess
}
