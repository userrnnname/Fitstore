package com.fitstore.data.domain

import com.fitstore.shared.domain.Order
import com.fitstore.shared.domain.Product

interface OrderRepository {
    suspend fun createOrderFromCart(
        userId: String,
        deliveryAddress: String,
        phoneNumber: String,
    ): Result<Order>
    suspend fun getLastPurchasedProducts(userId: String, limit: Int = 3): List<Product>
}