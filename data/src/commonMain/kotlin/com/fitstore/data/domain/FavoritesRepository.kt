package com.fitstore.data.domain

import com.fitstore.shared.domain.Product
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getCurrentUserId(): String?

    suspend fun addToFavorites(productId: String): Result<Unit>
    suspend fun removeFromFavorites(productId: String): Result<Unit>

    fun getFavoriteProductsFlow(): Flow<RequestState<List<Product>>>

    fun isFavoriteFlow(productId: String): Flow<Boolean>
}