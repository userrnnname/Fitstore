import com.fitstore.data.domain.FavoriteRepository
import com.fitstore.shared.domain.Favorite
import com.fitstore.shared.domain.Product
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FavoriteRepositoryImpl(
    private val supabase: SupabaseClient
) : FavoriteRepository {

    override fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun addToFavorites(productId: String): Result<Unit> = try {
        val userId = getCurrentUserId() ?: throw Exception("User not logged in")
        val favorite = Favorite(customerId = userId, productId = productId)
        supabase.from("favorites").insert(favorite)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeFromFavorites(productId: String): Result<Unit> = try {
        val userId = getCurrentUserId() ?: throw Exception("User not logged in")
        supabase.from("favorites").delete {
            filter {
                eq("customer_id", userId)
                eq("product_id", productId)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getFavoriteProductsFlow(): Flow<RequestState<List<Product>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(RequestState.Error("Пользователь не авторизован"))
            close()
            return@callbackFlow
        }

        trySend(RequestState.Loading)

        val channel = supabase.channel("fav_list_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "favorites"
            filter("user_id", FilterOperator.EQ, userId)
        }

        val job = launch {
            val initialResult = fetchProducts(userId)
            initialResult.onSuccess { trySend(RequestState.Success(it)) }
                .onFailure { trySend(RequestState.Error(it.message ?: "Error")) }
            changes.collect {
                fetchProducts(userId)
                    .onSuccess { trySend(RequestState.Success(it)) }
                    .onFailure { trySend(RequestState.Error(it.message ?: "Error")) }
            }
        }

        channel.subscribe()

        awaitClose {
            launch { channel.unsubscribe() }
            job.cancel()
        }
    }.distinctUntilChanged()

    override fun isFavoriteFlow(productId: String): Flow<Boolean> = callbackFlow {
        val userId = getCurrentUserId() ?: return@callbackFlow flowOf(false).collect { trySend(it) }

        val channel = supabase.channel("fav_check_${productId}_$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "favorites"
            filter("user_id", FilterOperator.EQ, userId)
        }

        suspend fun updateFavoriteStatus() {
            trySend(checkIsFavorite(userId, productId))
        }
        val job = launch {
            updateFavoriteStatus()
            changes.collect {
                updateFavoriteStatus()
            }
        }

        channel.subscribe()

        awaitClose {
            launch { channel.unsubscribe() }
            job.cancel()
        }
    }.distinctUntilChanged()

    private suspend fun fetchProducts(userId: String): Result<List<Product>> = try {
        val response = supabase.from("favorites")
            .select(columns = Columns.raw("*, product:products(*)")) {
                filter { eq("customer_id", userId) }
            }
            .decodeList<FavoriteWithProductResponse>()
        Result.success(response.mapNotNull { it.product })
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun checkIsFavorite(userId: String, productId: String): Boolean = try {
        val response = supabase.from("favorites").select {
            filter {
                eq("customer_id", userId)
                eq("product_id", productId)
            }
            limit(1)
        }
        response.data != "[]"
    } catch (e: Exception) {
        false
    }
}
@Serializable
private data class FavoriteWithProductResponse(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("added_at") val addedAt: String?,
    val product: Product?
)