package com.fitstore.data

import com.fitstore.data.domain.SupplementRepository
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class SupplementRepositoryImpl(
    private val supabase: SupabaseClient
) : SupplementRepository {
    override fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun updateSupplementTrack(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.from("supplement_tracks").update(track) {
                filter { eq("id", track.id ?: "") }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка при обновлении курса: ${e.message}")
        }
    }

    override fun readSupplementTracksFlow(): Flow<RequestState<List<SupplementTrack>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(RequestState.Error("Пользователь не авторизован"))
            close()
            return@callbackFlow
        }
        val channel = supabase.channel("supplement-tracks-$userId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "supplement_tracks"
        }
        val job = launch {
            changes.collect {
                val tracks = supabase.from("supplement_tracks")
                    .select { filter { eq("customer_id", userId) } }
                    .decodeList<SupplementTrack>()
                trySend(RequestState.Success(tracks))
            }
        }
        channel.subscribe()
        val initialTracks = supabase.from("supplement_tracks")
            .select { filter { eq("customer_id", userId) } }
            .decodeList<SupplementTrack>()
        trySend(RequestState.Success(initialTracks))
        awaitClose {
            job.cancel()
            CoroutineScope(Dispatchers.IO).launch { supabase.realtime.removeChannel(channel) }
        }
    }

    override suspend fun takeServing(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val currentTime = Clock.System.now().toEpochMilliseconds().toString()
            supabase.postgrest.rpc(
                function = "take_serving",
                parameters = mapOf(
                    "p_track_id" to (track.id ?: ""),
                    "p_current_time" to currentTime
                )
            )
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка сервера: ${e.message}")
        }
    }

    override suspend fun addSupplementTrack(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.from("supplement_tracks").insert(track)
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка при добавлении курса: ${e.message}")
        }
    }

    override suspend fun deleteSupplementTrack(
        trackId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.from("supplement_tracks").delete {
                filter { eq("id", trackId) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка при удалении: ${e.message}")
        }
    }
}