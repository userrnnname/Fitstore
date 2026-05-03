package com.fitstore.data.domain

import com.fitstore.shared.domain.Customer
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCurrentUserId(): String?
    fun observeUserId(): Flow<String?>
    suspend fun getCurrentCustomer(): RequestState<Customer>

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
    suspend fun signOut(): RequestState<Unit>
}