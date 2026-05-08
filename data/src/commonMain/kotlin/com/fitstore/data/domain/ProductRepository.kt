package com.fitstore.data.domain

import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.domain.ReviewWithCustomer
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getCurrentUserId(): String?
    fun readDiscountedProducts(): Flow<RequestState<List<Product>>>
    fun readNewProducts(): Flow<RequestState<List<Product>>>
    fun readProductByIdFlow(
        id: String
    ): Flow<RequestState<Product>>
    fun readProductsByIdsFlow(
        ids: List<String>
    ): Flow<RequestState<List<Product>>>
    fun readProductsByCategoryFlow(
        category: ProductCategory
    ): Flow<RequestState<List<Product>>>
    fun filterProducts(
        category: ProductCategory? = null,
        brand: String? = null,
        searchComposition: String? = null,
        tags: List<String>? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): Flow<RequestState<List<Product>>>
    suspend fun addReview(
        productId: String,
        customerId: String,
        rating: Int,
        comment: String?
    ): Result<Unit>
    fun getProductReviews(productId: String): Flow<RequestState<List<ReviewWithCustomer>>>
    suspend fun deleteReview(
        reviewId: String,
        customerId: String
    ): Result<Unit>
}
