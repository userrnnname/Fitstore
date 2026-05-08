package com.fitstore.shared.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Favorite(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("added_at") val addedAt: String? = null
)


@Serializable
data class FavoriteWithProduct(
    val favorite: Favorite,
    val product: Product
)