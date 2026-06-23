package de.servicehealth.poppmodule.demo.thirdparty

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.Res
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_allow
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_denied
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_open_settings
import de.servicehealth.poppmodule.demo.thirdparty.generated.resources.camera_permission_rationale
import de.servicehealth.poppmodule.sdk.qr.ScanResult
import de.servicehealth.poppmodule.theme.BrandTheme
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun QrCameraViewfinder(
    onResult: (ScanResult) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    if (LocalInspectionMode.current) {
        LaunchedEffect(Unit) { onActiveChange(true) }
        Box(modifier.background(Color.Black))
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = viewModel { QrScannerViewModel(context.applicationContext) }
    val c = BrandTheme.colors

    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnActiveChange by rememberUpdatedState(onActiveChange)

    val surfaceRequest by viewModel.surfaceRequests.collectAsState()
    val active = viewModel.permissionStatus == CameraPermissionStatus.Granted && surfaceRequest != null
    LaunchedEffect(active) { currentOnActiveChange(active) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val canAskAgain =
                granted ||
                    (context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ?: false)
            viewModel.onPermissionResult(granted, canAskAgain)
        }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshPermission()
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel.permissionStatus, lifecycleOwner) {
        when (viewModel.permissionStatus) {
            CameraPermissionStatus.Granted -> viewModel.bindCamera(lifecycleOwner)
            CameraPermissionStatus.Denied -> permissionLauncher.launch(Manifest.permission.CAMERA)
            CameraPermissionStatus.PermanentlyDenied -> Unit
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.results.collect { currentOnResult(it) }
    }

    when (viewModel.permissionStatus) {
        CameraPermissionStatus.Granted -> {
            val request = surfaceRequest
            if (request != null) {
                CameraXViewfinder(
                    surfaceRequest = request,
                    modifier = modifier,
                    implementationMode = ImplementationMode.EMBEDDED,
                )
            } else {
                Box(modifier.background(c.deep))
            }
        }

        CameraPermissionStatus.Denied ->
            PermissionPanel(
                modifier = modifier,
                message = stringResource(Res.string.camera_permission_rationale),
                actionLabel = stringResource(Res.string.camera_permission_allow),
                onAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )

        CameraPermissionStatus.PermanentlyDenied ->
            PermissionPanel(
                modifier = modifier,
                message = stringResource(Res.string.camera_permission_denied),
                actionLabel = stringResource(Res.string.camera_permission_open_settings),
                onAction = { context.findActivity()?.let { openAppSettings(it) } },
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

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun openAppSettings(activity: Activity) {
    val intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null),
        )
    activity.startActivity(intent)
}
