package com.fitstore.data.domain

import com.fitstore.shared.domain.PaymentItem

interface PaymentRepository {
    suspend fun preparePayment(amount: Double, orderId: String, items: List<PaymentItem>): Result<String>
}