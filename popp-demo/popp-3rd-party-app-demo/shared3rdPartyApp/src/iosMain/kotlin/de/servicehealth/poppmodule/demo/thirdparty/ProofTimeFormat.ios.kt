package de.servicehealth.poppmodule.demo.thirdparty

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970

private val proofTimeFormatter: NSDateFormatter =
    NSDateFormatter().apply {
        locale = NSLocale("de_DE")
        dateFormat = "dd.MM.yyyy · HH:mm 'Uhr'"
    }

actual fun formatProofTime(epochSeconds: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())
    return proofTimeFormatter.stringFromDate(date)
}
