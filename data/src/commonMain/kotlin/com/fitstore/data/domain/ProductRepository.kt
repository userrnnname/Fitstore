package com.fitstore.data.domain

import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getCurrentUserId(): String?
    fun readDiscountedProducts(): Flow<RequestState<List<Product>>>
    fun readNewProducts(): Flow<RequestState<List<Product>>>
    fun readProductByIdFlow(id: String): Flow<RequestState<Product>>
    fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>>
    fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>>
}
