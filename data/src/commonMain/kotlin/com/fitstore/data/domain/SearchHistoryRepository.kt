package com.fitstore.data.domain

import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getCurrentUserId(): String?

    suspend fun saveQuery(query: String)

    fun getRecentQueries(limit: Int = 5): Flow<RequestState<List<String>>>

    suspend fun clearHistory()

    suspend fun deleteQuery(query: String)
}