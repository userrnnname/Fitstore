package com.fitstore.data.domain

import com.fitstore.data.CartItemWithProduct
import com.fitstore.shared.domain.Cart
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    suspend fun getOrCreateCart(userId: String): Cart
    fun getCartItemsWithProductsFlow(userId: String): Flow<List<CartItemWithProduct>>
    suspend fun addOrUpdateItem(
        cartId: String,
        productId: String,
        flavor: String?,
        quantity: Int)
    suspend fun updateItemQuantity(
        cartItemId: String,
        newQuantity: Int)
    suspend fun removeItem(cartItemId: String)
    suspend fun clearCart(cartId: String?)
}