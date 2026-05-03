package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupplementTrack(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_title") val productTitle: String,
    @SerialName("product_thumbnail") val productThumbnail: String,
    @SerialName("total_servings") val totalServings: Int,
    @SerialName("remaining_servings") val remainingServings: Int,
    @SerialName("last_taken_date") val lastTakenDate: String? = null
)