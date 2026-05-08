package com.fitstore.data

import com.fitstore.data.domain.ProductRepository
import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.domain.Review
import com.fitstore.shared.domain.ReviewWithCustomer
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    override fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override fun readDiscountedProducts(): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val response = supabase.postgrest["products"]
                .select {
                    filter { eq("is_discounted", true) }
                }
                .decodeList<Product>()
            emit(RequestState.Success(response))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка чтения товаров со скидкой: ${e.message}"))
        }
    }

    override fun readNewProducts(): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val response = supabase.postgrest["products"]
                .select {
                    filter { eq("is_new", true) }
                }
                .decodeList<Product>()
            emit(RequestState.Success(response))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка чтения новых товаров: ${e.message}"))
        }
    }

    override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> = flow {
        emit(RequestState.Loading)
        try {
            val product = supabase.postgrest["products"]
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Product>()
            emit(RequestState.Success(product))
        } catch (e: Exception) {
            emit(RequestState.Error("Товар не найден: ${e.message}"))
        }
    }

    override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        if (ids.isEmpty()) {
            emit(RequestState.Success(emptyList()))
            return@flow
        }
        try {
            val response = supabase.postgrest["products"]
                .select {
                    filter { isIn("id", ids) }
                }
                .decodeList<Product>()
            emit(RequestState.Success(response))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка товаров: ${e.message}"))
        }
    }

    override fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val response = supabase.postgrest["products"]
                .select {
                    filter { eq("category", category.name) }
                }
                .decodeList<Product>()
            emit(RequestState.Success(response))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка чтения категорий: ${e.message}"))
        }
    }

    override fun filterProducts(
        category: ProductCategory?,
        brand: String?,
        searchComposition: String?,
        tags: List<String>?,
        minPrice: Double?,
        maxPrice: Double?
    ): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val result = supabase.from("products").select {
                filter {
                    category?.let { eq("category", it.name) }
                    brand?.let { eq("brand", it) }
                    searchComposition?.let { ilike("composition", "%$it%") }
                    tags?.takeIf { it.isNotEmpty() }?.let { contains("tags", it) }
                    minPrice?.let { gte("price", it) }
                    maxPrice?.let { lte("price", it) }
                }
            }.decodeList<Product>()

            emit(RequestState.Success(result))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка фильтрации: ${e.message}"))
        }
    }

    override suspend fun addReview(
        productId: String,
        customerId: String,
        rating: Int,
        comment: String?
    ): Result<Unit> = try {
        val review = Review(
            productId = productId,
            customerId = customerId,
            rating = rating,
            comment = comment
        )
        supabase.from("reviews").insert(review)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteReview(reviewId: String, customerId: String): Result<Unit> = try {
        supabase.from("reviews").delete {
            filter {
                eq("id", reviewId)
                eq("customer_id", customerId)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getProductReviews(productId: String): Flow<RequestState<List<ReviewWithCustomer>>> = flow {
        emit(RequestState.Loading)
        try {
            val response = supabase.from("reviews").select(
                columns = Columns.raw("""
                *,
                customers(first_name, last_name)
            """)
            ) {
                filter { eq("product_id", productId) }
                order("created_at", order = Order.DESCENDING)
            }.decodeList<ReviewWithCustomerResponse>()

            val reviews = response.map { resp ->
                ReviewWithCustomer(
                    review = Review(
                        id = resp.id,
                        productId = resp.product_id,
                        customerId = resp.customer_id,
                        rating = resp.rating,
                        comment = resp.comment,
                        createdAt = resp.created_at,
                        updatedAt = resp.updated_at
                    ),
                    firstName = resp.customers?.first_name ?: "Аноним",
                    lastName = resp.customers?.last_name ?: ""
                )
            }
            emit(RequestState.Success(reviews))
        } catch (e: Exception) {
            emit(RequestState.Error("Не удалось загрузить отзывы: ${e.message}"))
        }
    }
}

@Serializable
private data class ReviewWithCustomerResponse(
    val id: String,
    val product_id: String,
    val customer_id: String,
    val rating: Int,
    val comment: String?,
    val created_at: String?,
    val updated_at: String?,
    val customers: CustomerInfo?
) {
    @Serializable
    data class CustomerInfo(
        @SerialName("first_name") val first_name: String,
        @SerialName("last_name") val last_name: String
    )
}