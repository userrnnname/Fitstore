package com.fitstore.data

import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.notifications.PlatformNotification
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CustomerRepositoryImpl(
    private val supabase: SupabaseClient,
    private val notifications: PlatformNotification,
) : CustomerRepository {

    override fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id
    override suspend fun getCurrentCustomer(): RequestState<Customer> {
        val userId = getCurrentUserId() ?: return RequestState.Error("Пользователь не авторизован")
        return try {
            val customer = supabase.from("customers")
                .select { filter { eq("id", userId) } }
                .decodeSingle<Customer>()
            RequestState.Success(customer)
        } catch (e: Exception) {
            RequestState.Error("Не удалось загрузить профиль: ${e.message}")
        }
    }

    override fun observeUserId(): Flow<String?> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.id
            else -> null
        }
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
                city = null,
                postalCode = null,
                address = null,
                phoneNumber = null,
                isAdmin = false
            )
            supabase.from("customers").insert(customer)
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка создания пользователя: ${e.message}")
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
        val userId = getCurrentUserId()
        if (userId == null) {
            emit(RequestState.Error("Пользователь не авторизован"))
            return@flow
        }
        try {
            val customer = supabase.from("customers")
                .select { filter { eq("id", userId) } }
                .decodeSingle<Customer>()
            emit(RequestState.Success(customer))
        } catch (e: Exception) {
            emit(RequestState.Error("Не удалось загрузить профиль: ${e.message}"))
        }
    }

    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.from("customers").update(customer) {
                filter { eq("id", customer.id!!) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка обновления: ${e.message}")
        }
    }

    override suspend fun signOut(): RequestState<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val token = notifications.getToken()
                if (token != null) {
                    supabase.from("fcm_tokens").delete {
                        filter {
                            eq("user_id", userId)
                            eq("token", token)
                        }
                    }
                }
            }
            supabase.auth.signOut()
            RequestState.Success(Unit)
        } catch (e: Exception) {
            RequestState.Error("Ошибка выхода: ${e.message}")
        }
    }
}