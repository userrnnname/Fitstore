package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null
)