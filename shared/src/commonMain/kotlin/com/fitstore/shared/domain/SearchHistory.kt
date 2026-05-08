package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchHistoryEntry(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    val query: String,
    @SerialName("searched_at") val searchedAt: String? = null
)