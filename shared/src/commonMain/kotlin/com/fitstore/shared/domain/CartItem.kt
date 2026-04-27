package com.fitstore.shared.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class CartItem(
    val id: String? = null,
    val productId: String,
    val flavor: String? = null,
    val quantity: Int
)