package com.fitstore.shared.util

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