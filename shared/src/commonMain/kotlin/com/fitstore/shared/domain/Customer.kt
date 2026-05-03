package com.fitstore.shared.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String? = null,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    val city: String? = null,
    @SerialName("postal_code") val postalCode: Int? = null,
    val address: String? = null,
    @SerialName("phone_number") val phoneNumber: PhoneNumber? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false
)
@Serializable
data class PhoneNumber(
        @SerialName("dial_code") val dialCode: Int,
        val number: String
        )
