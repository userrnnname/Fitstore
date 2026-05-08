package com.fitstore.shared.payment

sealed class PaymentResult {
    data class Success(val paymentToken: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
    object Cancelled : PaymentResult()
}
