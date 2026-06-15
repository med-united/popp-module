package de.servicehealth.poppmodule.demo.thirdparty

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraPermissionTest {

    @Test
    fun grantedMapsToGranted() {
        assertEquals(
            CameraPermissionStatus.Granted,
            resolveCameraPermission(granted = true, canAskAgain = false),
        )
    }

    @Test
    fun grantedTakesPrecedenceEvenIfCanAskAgain() {
        assertEquals(
            CameraPermissionStatus.Granted,
            resolveCameraPermission(granted = true, canAskAgain = true),
        )
    }

    @Test
    fun deniedButCanAskAgainMapsToDenied() {
        assertEquals(
            CameraPermissionStatus.Denied,
            resolveCameraPermission(granted = false, canAskAgain = true),
        )
    }

    @Test
    fun deniedAndCannotAskAgainMapsToPermanentlyDenied() {
        assertEquals(
            CameraPermissionStatus.PermanentlyDenied,
            resolveCameraPermission(granted = false, canAskAgain = false),
        )
    }
}
