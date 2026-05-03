package com.fitstore.data

import com.fitstore.data.domain.ProductRepository
import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
}