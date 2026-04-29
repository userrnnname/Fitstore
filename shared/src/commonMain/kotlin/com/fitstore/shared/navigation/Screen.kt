package com.fitstore.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Auth : Screen()
    @Serializable
    data object  Login : Screen()
    @Serializable
    data object  Register : Screen()
    @Serializable
    data object  HomeGraph : Screen()
    @Serializable
    data object  ProductsOverview : Screen()
    @Serializable
    data object  Cart : Screen()
    @Serializable
    data object  Categories : Screen()
    @Serializable
    data class CategorySearch(
        val category: String
    ) : Screen()
    @Serializable
    data object  Profile : Screen()
    @Serializable
    data object  EditProfile : Screen()
    @Serializable
    data object  AdminPanel : Screen()
    @Serializable
    data class  ManageProduct (
        val id: String? = null,
    ) : Screen()
    @Serializable
    data class Details(
        val id: String
    ) : Screen()
    @Serializable
    data object Checkout : Screen()
    @Serializable
    data class PaymentCompleted(
        val isSuccess: Boolean? = null,
        val error: String? = null,
        val totalAmount: Double? = null
    ) : Screen()
}