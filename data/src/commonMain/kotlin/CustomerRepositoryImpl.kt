package com.fitstore.data

import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CustomerRepositoryImpl(
    private val supabase: SupabaseClient
) : CustomerRepository {

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override suspend fun createCustomer(
        user: UserInfo,
        lastName: String?,
        firstName: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val customer = Customer(
                id = user.id,
                email = user.email ?: "",
                firstName = firstName ?: "",
                lastName = lastName ?: "",
                cart = emptyList(),
                isAdmin = false
            )

            supabase.postgrest.from("customers").insert(customer)
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка создания профиля: ${e.message}")
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка входа: ${e.message}")
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        lastName: String,
        firstName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val result = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userInfo = supabase.auth.currentUserOrNull()

            if (userInfo != null) {
                createCustomer(
                    user = userInfo,
                    lastName = lastName,
                    firstName = firstName,
                    onSuccess = onSuccess,
                    onError = onError
                )
            } else {
                onSuccess()
            }

        } catch (e: Exception) {
            onError("Ошибка регистрации: ${e.message}")
        }
    }

    override fun readCustomerFlow(): Flow<RequestState<Customer>> = flow {
        emit(RequestState.Loading)
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val customer = supabase.postgrest.from("customers")
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingle<Customer>()
                emit(RequestState.Success(customer))
            } else {
                emit(RequestState.Error("Пользователь не найден."))
            }
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка загрузки профиля: ${e.message}"))
        }
    }

    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.postgrest.from("customers").update(customer) {
                filter { eq("id", customer.id) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка обновления.: ${e.message}")
        }
    }

    override suspend fun signOut(): RequestState<Unit> {
        return try {
            supabase.auth.signOut()
            RequestState.Success(Unit)
        } catch (e: Exception) {
            RequestState.Error("Ошибка выхода: ${e.message}")
        }
    }
}
