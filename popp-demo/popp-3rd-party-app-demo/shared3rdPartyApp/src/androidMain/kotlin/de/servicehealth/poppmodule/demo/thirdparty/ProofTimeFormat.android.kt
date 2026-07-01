package de.servicehealth.poppmodule.demo.thirdparty

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val proofTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy · HH:mm 'Uhr'", Locale.GERMANY)

actual fun formatProofTime(epochSeconds: Long): String =
    Instant.ofEpochSecond(epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(proofTimeFormatter)
