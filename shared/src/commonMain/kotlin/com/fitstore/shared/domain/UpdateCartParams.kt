package com.fitstore.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCartParams(
    val p_customer_id: String,
    val p_item_id: String,
    val p_new_quantity: Int
)