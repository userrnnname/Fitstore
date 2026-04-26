package com.fitstore.data.domain

import com.fitstore.shared.domain.CartItem
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCurrentUserId(): String?

    suspend fun createCustomer(
        user: UserInfo,
        lastName: String? = null,
        firstName: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        lastName: String,
        firstName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun readCustomerFlow(): Flow<RequestState<Customer>>

    suspend fun addItemToCard(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun signOut(): RequestState<Unit>
}