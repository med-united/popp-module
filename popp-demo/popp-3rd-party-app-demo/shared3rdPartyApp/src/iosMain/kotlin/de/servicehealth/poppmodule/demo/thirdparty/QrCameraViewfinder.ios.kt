package de.servicehealth.poppmodule.demo.thirdparty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.compose.LifecycleResumeEffect
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_denied
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_open_settings
import de.servicehealth.poppmodule.sdk.qr.IosQrScanner
import de.servicehealth.poppmodule.sdk.qr.ScanResult
import de.servicehealth.poppmodule.theme.BrandTheme
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import org.jetbrains.compose.resources.stringResource
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private enum class CameraAuth { NotDetermined, Authorized, Denied }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrCameraViewfinder(
    onResult: (ScanResult) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val c = BrandTheme.colors
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnActiveChange by rememberUpdatedState(onActiveChange)

    val scanner = remember { IosQrScanner() }
    var status by remember { mutableStateOf(cameraAuthStatus()) }

    DisposableEffect(scanner) {
        onDispose { scanner.close() }
    }

    LifecycleResumeEffect(Unit) {
        if (status != CameraAuth.Authorized) status = cameraAuthStatus()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        if (status == CameraAuth.NotDetermined) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                dispatch_async(dispatch_get_main_queue()) {
                    status = if (granted) CameraAuth.Authorized else CameraAuth.Denied
                }
            }
        }
    }

    LaunchedEffect(status) {
        currentOnActiveChange(status == CameraAuth.Authorized && scanner.start())
    }

    LaunchedEffect(scanner) {
        scanner.results.collect { currentOnResult(it) }
    }

    when (status) {
        CameraAuth.Authorized ->
            UIKitView(
                factory = { CameraPreviewView(AVCaptureVideoPreviewLayer(session = scanner.session)) },
                modifier = modifier,
            )

        CameraAuth.NotDetermined -> Box(modifier.background(c.deep))

        CameraAuth.Denied ->
            PermissionPanel(
                modifier = modifier,
                message = stringResource(Res.string.camera_permission_denied),
                actionLabel = stringResource(Res.string.camera_permission_open_settings),
                onAction = { openAppSettings() },
            )
    }
}

@Composable
private fun PermissionPanel(
    modifier: Modifier,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val c = BrandTheme.colors
    Box(modifier.background(c.deep), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = message,
                color = c.white.copy(alpha = 0.76f),
                style = BrandTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = c.yellow,
                    style = BrandTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun cameraAuthStatus(): CameraAuth =
    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusAuthorized -> CameraAuth.Authorized
        AVAuthorizationStatusNotDetermined -> CameraAuth.NotDetermined
        else -> CameraAuth.Denied
    }

private fun openAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class CameraPreviewView(
    private val previewLayer: AVCaptureVideoPreviewLayer,
) : UIView(frame = CGRectZero.readValue()) {
    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
    }
}
