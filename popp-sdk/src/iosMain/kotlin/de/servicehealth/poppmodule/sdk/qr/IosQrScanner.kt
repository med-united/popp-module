package de.servicehealth.poppmodule.sdk.qr

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionDidStartRunningNotification
import platform.AVFoundation.AVCaptureSessionDidStopRunningNotification
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
class IosQrScanner : PoppQrScanner {
    private val _results =
        MutableSharedFlow<ScanResult>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val results: Flow<ScanResult> = _results.asSharedFlow()

    val session: AVCaptureSession = AVCaptureSession()

    private val delegate =
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection,
            ) {
                val code =
                    didOutputMetadataObjects
                        .firstNotNullOfOrNull { it as? AVMetadataMachineReadableCodeObject } ?: return
                val value = code.stringValue
                if (value == null) {
                    _results.tryEmit(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_UTF8))
                    return
                }
                _results.tryEmit(parseCheckInPayload(value))
            }
        }

    private val _isActive = MutableStateFlow(false)

    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val startObserver =
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVCaptureSessionDidStartRunningNotification,
            `object` = session,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _ -> _isActive.value = true },
        )

    private val stopObserver =
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVCaptureSessionDidStopRunningNotification,
            `object` = session,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _ -> _isActive.value = false },
        )

    private val sessionQueue = dispatch_queue_create("de.servicehealth.poppmodule.qr.session", null)

    private var configured = false

    fun start() {
        dispatch_async(sessionQueue) {
            if (!configured && !configure()) return@dispatch_async
            session.startRunning()
        }
    }

    private fun configure(): Boolean {
        val device =
            AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
                deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
                mediaType = AVMediaTypeVideo,
                position = AVCaptureDevicePositionBack,
            ).devices.firstOrNull() as? AVCaptureDevice ?: return false
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) ?: return false
        if (!session.canAddInput(input)) return false

        val output = AVCaptureMetadataOutput()
        if (!session.canAddOutput(output)) return false

        session.beginConfiguration()
        session.addInput(input)
        session.addOutput(output)
        output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        session.commitConfiguration()

        configured = true
        return true
    }

    fun stop() {
        dispatch_async(sessionQueue) { session.stopRunning() }
    }

    override fun close() {
        NSNotificationCenter.defaultCenter.removeObserver(startObserver)
        NSNotificationCenter.defaultCenter.removeObserver(stopObserver)
        dispatch_async(sessionQueue) {
            session.stopRunning()
            session.inputs.forEach { session.removeInput(it as AVCaptureInput) }
            session.outputs.forEach { session.removeOutput(it as AVCaptureOutput) }
            configured = false
        }
    }
}
