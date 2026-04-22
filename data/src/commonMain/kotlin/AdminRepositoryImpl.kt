package com.fitstore.data

import com.fitstore.data.domain.AdminRepository
import com.fitstore.data.domain.ImageRepository
import com.fitstore.shared.domain.Product
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AdminRepositoryImpl(
    private val imageRepository: ImageRepository,
    private val supabase: SupabaseClient
) : AdminRepository {

    companion object {
        private const val MAX_IMAGE_SIZE_BYTES = 5_242_880L
    }

    override fun getCurrentUserId() = supabase.auth.currentUserOrNull()?.id

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun uploadImageToStorage(byteArray: ByteArray): String? {
        return try {
            if (getCurrentUserId() == null) return null
            if (byteArray.size > MAX_IMAGE_SIZE_BYTES) return null

            val fileName = Uuid.random().toHexString()
            imageRepository.uploadImage(
                fileName = fileName,
                byteArray = byteArray,
                bucketName = "product-images"
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteImageFromStorage(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fileName = downloadUrl.substringAfterLast("/").substringBeforeLast("?")
            imageRepository.deleteImage(
                fileName = fileName,
                bucketName = "product-images"
            )
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка при удалении: ${e.message}")
        }
    }

    override suspend fun createNewProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (getCurrentUserId() != null) {
                supabase.postgrest.from("products").insert(product.copy(title = product.title.lowercase()))
                onSuccess()
            } else onError("Пользователь недоступен.")
        } catch (e: Exception) {
            onError("Ошибка при создании: ${e.message}")
        }
    }

    override suspend fun readProductById(id: String): RequestState<Product> {
        return try {
            val product = supabase.postgrest.from("products")
                .select { filter { eq("id", id) } }
                .decodeSingle<Product>()
            RequestState.Success(product.copy(title = product.title.uppercase()))
        } catch (e: Exception) {
            RequestState.Error("Товар не найден: ${e.message}")
        }
    }

    override suspend fun updateProductThumbnail(
        productId: String,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.postgrest.from("products").update(
                {
                    set("thumbnail", downloadUrl)
                }
            ) {
                filter {
                    eq("id", productId)
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка обновления миниатюры: ${e.message}")
        }
    }

    override suspend fun updateProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.postgrest.from("products").update(product.copy(title = product.title.lowercase())) {
                filter { eq("id", product.id) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка обновления: ${e.message}")
        }
    }

    override suspend fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.postgrest.from("products").delete {
                filter { eq("id", productId) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка удаления: ${e.message}")
        }
    }

    override fun readLastTenProducts(): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val products = supabase.postgrest.from("products")
                .select {
                    order("createdAt", Order.DESCENDING)
                    limit(10)
                }.decodeList<Product>()
            emit(RequestState.Success(products.map { it.copy(title = it.title.uppercase()) }))
        } catch (e: Exception) {
            emit(RequestState.Error(e.message ?: "Ошибка загрузки"))
        }
    }

    override fun searchProductsByTitle(searchQuery: String): Flow<RequestState<List<Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val products = supabase.postgrest.from("products")
                .select {
                    filter { ilike("title", "%${searchQuery.lowercase()}%") }
                }.decodeList<Product>()
            emit(RequestState.Success(products.map { it.copy(title = it.title.uppercase()) }))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка поиска: ${e.message}"))
        }
    }
}
