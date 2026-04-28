package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customerId")
    val customerId: String,
    val items: List<CartItem>,
    @SerialName("totalAmount")
    val totalAmount: Double,
    @SerialName("createdAt")
    val createdAt: String? = null
)