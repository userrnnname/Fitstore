package com.fitstore.data

import com.fitstore.data.domain.SupplementRepository
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

class SupplementRepositoryImpl(
    private val supabase: SupabaseClient
) : SupplementRepository {
    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

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

    override fun readSupplementTracksFlow(): Flow<RequestState<List<SupplementTrack>>> = flow {
        emit(RequestState.Loading)
        val userId = getCurrentUserId() ?: return@flow emit(RequestState.Error("Пользователь не авторизован."))

        try {
            val tracks = supabase.from("supplement_tracks")
                .select {
                    filter { eq("customerId", userId) }
                }.decodeList<SupplementTrack>()

            emit(RequestState.Success(tracks))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка загрузки: ${e.message}"))
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