package de.servicehealth.poppmodule.demo.thirdparty

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatProofTime(epochSeconds: Long): String {
    val formatter = NSDateFormatter().apply { dateFormat = "dd.MM.yyyy · HH:mm 'Uhr'" }
    val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())
    return formatter.stringFromDate(date)
}
