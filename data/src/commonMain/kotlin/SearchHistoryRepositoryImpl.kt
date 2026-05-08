import com.fitstore.data.domain.SearchHistoryRepository
import com.fitstore.shared.domain.SearchHistoryEntry
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class SearchHistoryRepositoryImpl(
    private val supabase: SupabaseClient
) : SearchHistoryRepository {

    override fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun saveQuery(query: String) {
        val userId = getCurrentUserId() ?: return
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return

        try {
            supabase.from("search_history").delete {
                filter {
                    eq("customer_id", userId)
                    eq("query", trimmedQuery)
                }
            }

            val entry = SearchHistoryEntry(
                customerId = userId,
                query = trimmedQuery
            )
            supabase.from("search_history").insert(entry)

            cleanupExcessEntries(userId, maxEntries = 20)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun cleanupExcessEntries(userId: String, maxEntries: Int) {
        try {
            val entries = supabase.from("search_history")
                .select {
                    filter { eq("customer_id", userId) }
                    order("searched_at", Order.DESCENDING)
                }
                .decodeList<SearchHistoryEntry>()

            if (entries.size > maxEntries) {
                val idsToDelete = entries.drop(maxEntries).mapNotNull { it.id }
                if (idsToDelete.isNotEmpty()) {
                    supabase.from("search_history").delete {
                        filter {
                            isIn("id", idsToDelete)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getRecentQueries(limit: Int): Flow<RequestState<List<String>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(RequestState.Error("Пользователь не авторизован"))
            close()
            return@callbackFlow
        }

        val channel = supabase.channel("history_updates")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "search_history"
            filter("customer_id", FilterOperator.EQ, userId)
        }

        suspend fun sendData() {
            try {
                val entries = supabase.from("search_history")
                    .select {
                        filter { eq("customer_id", userId) }
                        order("searched_at", Order.DESCENDING)
                        limit(limit.toLong())
                    }.decodeList<SearchHistoryEntry>()
                trySend(RequestState.Success(entries.map { it.query }))
            } catch (e: Exception) {
                trySend(RequestState.Error(e.message ?: "Unknown error"))
            }
        }

        launch {
            sendData()
            changes.collect { sendData() }
        }

        channel.subscribe()
        awaitClose { launch { channel.unsubscribe() } }
    }


    override suspend fun clearHistory() {
        val userId = getCurrentUserId() ?: return
        try {
            supabase.from("search_history").delete {
                filter { eq("customer_id", userId) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override suspend fun deleteQuery(query: String) {
        val userId = getCurrentUserId() ?: return
        try {
            supabase.from("search_history").delete {
                filter {
                    eq("customer_id", userId)
                    eq("query", query.trim())
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}