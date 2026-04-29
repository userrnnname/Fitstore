package com.fitstore.shared.domain

data class YandexPayOrder(
    val id: String,
    val amount: String,
    val label: String,
    val items: List<YandexOrderItem>
)

data class YandexOrderItem(
    val label: String,
    val amount: String,
    val quantity: Int = 1
)

data class PaymentTokenRequest(
    val orderId: String,
    val paymentToken: String,
    val totalAmount: Double
)

data class PaymentResult(
    val success: Boolean,
    val message: String? = null,
    val transactionId: String? = null
)