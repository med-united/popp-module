package de.servicehealth.poppmodule.sdk.qr

import kotlinx.coroutines.flow.Flow

interface PoppQrScanner {

    val results: Flow<ScanResult>

    fun close()
}
