package com.fitstore.shared.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Double.formatPrice(): String {
    val longValue = this.toLong()
    val stringValue = longValue.toString()
    val result = StringBuilder()

    val length = stringValue.length
    for (i in 0 until length) {
        result.append(stringValue[i])
        if ((length - i - 1) % 3 == 0 && i != length - 1) {
            result.append(" ")
        }
    }
    return result.toString()
}

fun Int.formatPrice(): String = this.toDouble().formatPrice()

fun Long.formatTimestamp(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')

    return "$day.$month $hour:$minute"
}