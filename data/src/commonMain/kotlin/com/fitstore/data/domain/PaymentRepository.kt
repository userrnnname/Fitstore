package com.fitstore.data.domain

interface PaymentRepository {
    suspend fun confirmPayment(
        orderId: String,
        paymentToken: String,
        amount: Double
    ): Result<Unit>

    suspend fun sendEmailReceipt(
        orderId: String,
        email: String,
        amount: Double,
        method: String)
}