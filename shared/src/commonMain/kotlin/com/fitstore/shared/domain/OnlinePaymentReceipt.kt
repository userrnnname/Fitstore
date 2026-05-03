package com.fitstore.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class PaymentItem(
    val title: String,
    val price: Double,
    val quantity: Int
)


@Serializable
data class PaymentRequest(
    val amount: Double,
    val orderId: String,
    val items: List<PaymentItem>
)