package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupplementTrack(
    val id: String? = null,
    @SerialName("customerId")
    val customerId: String,
    @SerialName("productId")
    val productId: String,
    @SerialName("productTitle")
    val productTitle: String,
    @SerialName("productThumbnail")
    val productThumbnail: String,
    @SerialName("totalServings")
    val totalServings: Int,
    @SerialName("remainingServings")
    val remainingServings: Int,
    @SerialName("lastTakenDate")
    val lastTakenDate: String? = null
)