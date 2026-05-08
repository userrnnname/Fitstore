package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String? = null,
    @SerialName("product_id") val productId: String,
    @SerialName("customer_id") val customerId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ReviewWithCustomer(
    val review: Review,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String
)