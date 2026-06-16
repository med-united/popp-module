package de.servicehealth.poppmodule.sdk.qr

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class AndroidQrScanner(private val context: Context) : PoppQrScanner {
    private val _surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    val surfaceRequests: StateFlow<SurfaceRequest?> = _surfaceRequests.asStateFlow()

    private val _results =
        MutableSharedFlow<ScanResult>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val results: Flow<ScanResult> = _results.asSharedFlow()

    private val analysisExecutor = Executors.newSingleThreadExecutor()

    private val barcodeScanner =
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build(),
        )

    private var cameraProvider: ProcessCameraProvider? = null

    private var boundUseCases: Array<UseCase> = emptyArray()

    private val inFlightProxy = AtomicReference<ImageProxy?>(null)

    suspend fun bindToCamera(lifecycleOwner: LifecycleOwner) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider = provider

        val preview =
            Preview.Builder().build().apply {
                surfaceProvider = Preview.SurfaceProvider { request -> _surfaceRequests.update { request } }
            }

        val analysis =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply { setAnalyzer(analysisExecutor, ::analyze) }

        if (boundUseCases.isNotEmpty()) provider.unbind(*boundUseCases)

        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis,
        )
        boundUseCases = arrayOf(preview, analysis)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        inFlightProxy.set(imageProxy)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull() ?: return@addOnSuccessListener
                val rawBytes = barcode.rawBytes
                val rawValue = barcode.rawValue
                val result =
                    when {
                        rawBytes != null -> parseCheckInPayload(rawBytes)
                        rawValue != null -> parseCheckInPayload(rawValue)
                        else -> return@addOnSuccessListener
                    }
                _results.tryEmit(result)
            }
            .addOnCompleteListener {
                if (inFlightProxy.compareAndSet(imageProxy, null)) {
                    imageProxy.close()
                }
            }
    }

    override fun close() {
        cameraProvider?.unbind(*boundUseCases)
        boundUseCases = emptyArray()
        cameraProvider = null
        inFlightProxy.getAndSet(null)?.close()
        barcodeScanner.close()
        analysisExecutor.shutdown()
    }
}
