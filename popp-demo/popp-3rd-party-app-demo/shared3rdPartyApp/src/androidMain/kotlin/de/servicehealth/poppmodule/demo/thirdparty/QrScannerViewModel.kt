package de.servicehealth.poppmodule.demo.thirdparty

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import de.servicehealth.poppmodule.sdk.qr.AndroidQrScanner
import de.servicehealth.poppmodule.sdk.qr.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class CameraPermissionStatus { Granted, Denied, PermanentlyDenied }

class QrScannerViewModel(private val appContext: Context) : ViewModel() {
    var permissionStatus by mutableStateOf(readSystemStatus())
        private set

    private val scanner = AndroidQrScanner(appContext)

    val surfaceRequests: StateFlow<SurfaceRequest?> get() = scanner.surfaceRequests
    val results: Flow<ScanResult> get() = scanner.results

    fun refreshPermission() {
        permissionStatus = readSystemStatus()
    }

    fun onPermissionResult(
        granted: Boolean,
        canAskAgain: Boolean,
    ) {
        permissionStatus = resolveCameraPermission(granted, canAskAgain)
    }

    suspend fun bindCamera(lifecycleOwner: LifecycleOwner) {
        if (permissionStatus == CameraPermissionStatus.Granted) {
            scanner.bindToCamera(lifecycleOwner)
        }
    }

    private fun readSystemStatus(): CameraPermissionStatus =
        if (appContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            CameraPermissionStatus.Granted
        } else {
            CameraPermissionStatus.Denied
        }

    override fun onCleared() {
        scanner.close()
    }
}

internal fun resolveCameraPermission(
    granted: Boolean,
    canAskAgain: Boolean,
): CameraPermissionStatus =
    when {
        granted -> CameraPermissionStatus.Granted
        canAskAgain -> CameraPermissionStatus.Denied
        else -> CameraPermissionStatus.PermanentlyDenied
    }
