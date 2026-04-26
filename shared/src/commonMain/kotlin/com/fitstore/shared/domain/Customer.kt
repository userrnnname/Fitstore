package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("firstName")
    val firstName: String,
    val email: String,
    val city: String? = null,
    @SerialName("postalCode")
    val postalCode: Int? = null,
    val address: String? = null,
    @SerialName("phoneNumber")
    val phoneNumber: PhoneNumber? = null,
    val cart: List<CartItem> = emptyList(),
    @SerialName("isAdmin")
    val isAdmin: Boolean = false
)

@Serializable
data class PhoneNumber(
        @SerialName("dialCode")
        val dialCode: Int,
        val number: String
        )
