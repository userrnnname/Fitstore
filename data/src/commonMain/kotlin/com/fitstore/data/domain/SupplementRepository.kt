package com.fitstore.data.domain

import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface SupplementRepository {
    fun getCurrentUserId(): String?
    fun readSupplementTracksFlow(): Flow<RequestState<List<SupplementTrack>>>
    suspend fun updateSupplementTrack(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit)
    suspend fun deleteSupplementTrack(
        trackId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit)
    suspend fun addSupplementTrack(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit)
    suspend fun takeServing(
        track: SupplementTrack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit)

}