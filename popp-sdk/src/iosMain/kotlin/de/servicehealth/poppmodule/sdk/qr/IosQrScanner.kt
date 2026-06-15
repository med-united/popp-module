package de.servicehealth.poppmodule.sdk.qr

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
class IosQrScanner : PoppQrScanner {

    private val _results = MutableSharedFlow<ScanResult>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val results: Flow<ScanResult> = _results.asSharedFlow()

    val session: AVCaptureSession = AVCaptureSession()

    private val delegate = object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
        override fun captureOutput(
            output: AVCaptureOutput,
            didOutputMetadataObjects: List<*>,
            fromConnection: AVCaptureConnection,
        ) {
            val code = didOutputMetadataObjects
                .firstNotNullOfOrNull { it as? AVMetadataMachineReadableCodeObject } ?: return
            val value = code.stringValue
            if (value == null) {
                _results.tryEmit(ScanResult.Invalid(ScanResult.Invalid.Reason.NOT_UTF8))
                return
            }
            _results.tryEmit(parseCheckInPayload(value))
        }
    }

    private var configured = false

    private val sessionQueue = dispatch_queue_create("de.servicehealth.poppmodule.qr.session", null)

    fun start(): Boolean {
        if (!configured && !configure()) return false
        dispatch_async(sessionQueue) { session.startRunning() }
        return true
    }

    private fun configure(): Boolean {
        val device = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionBack,
        ).devices.firstOrNull() as? AVCaptureDevice ?: return false
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) ?: return false
        if (!session.canAddInput(input)) return false

        val output = AVCaptureMetadataOutput()
        session.beginConfiguration()
        session.addInput(input)
        if (!session.canAddOutput(output)) {
            session.commitConfiguration()
            return false
        }
        session.addOutput(output)
        output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        session.commitConfiguration()

        configured = true
        return true
    }

    override fun close() {
        dispatch_async(sessionQueue) {
            session.stopRunning()
            session.inputs.forEach { session.removeInput(it as AVCaptureInput) }
            session.outputs.forEach { session.removeOutput(it as AVCaptureOutput) }
        }
    }
}
