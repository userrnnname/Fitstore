package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class CartItem(
    val id: String? = null,
    @SerialName("cart_id") val cartId: String,
    @SerialName("product_id") val productId: String,
    val flavor: String? = null,
    val quantity: Int,
    @SerialName("added_at") val addedAt: String? = null
)