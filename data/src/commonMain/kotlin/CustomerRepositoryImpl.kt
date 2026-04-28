package com.fitstore.data

import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.domain.CartItem
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.domain.UpdateCartParams
import com.fitstore.shared.util.RequestState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
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
        val userId = getCurrentUserId() ?: return@flow emit(RequestState.Error("Пользователь не авторизован."))

        try {
            val customer = supabase.from("customers")
                .select {
                    filter { eq("id", userId) }
                }.decodeSingle<Customer>()

            emit(RequestState.Success(customer))
        } catch (e: Exception) {
            emit(RequestState.Error("Ошибка загрузки: ${e.message}"))
        }
    }


    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            supabase.from("customers").update(customer) {
                filter { eq("id", customer.id) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка обновления.: ${e.message}")
        }
    }

    override suspend fun addItemToCard(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId() ?: return onError("Пользователь не авторизован.")
            val customer = supabase.from("customers")
                .select { filter { eq("id", userId) } }
                .decodeSingle<Customer>()
            val updatedCart = customer.cart + cartItem
            supabase.from("customers").update(
                { set("cart", updatedCart) }
            ) {
                filter { eq("id", userId) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка добавления в корзину: ${e.message}")
        }
    }

    override suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId() ?: return onError("Пользователь не авторизован.")

            supabase.postgrest.rpc(
                function = "update_cart_item_quantity",
                parameters = UpdateCartParams(
                    p_customer_id = userId,
                    p_item_id = id,
                    p_new_quantity = quantity
                )
            )
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка сервера: ${e.message}")
        }
    }

    override suspend fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId() ?: return onError("Пользователь не авторизован.")

            val customer = supabase.from("customers")
                .select(Columns.list("cart")) {
                    filter { eq("id", userId) }
                }.decodeSingle<Customer>()

            val updatedCart = customer.cart.filterNot { it.id == id }

            supabase.from("customers").update(
                { set("cart", updatedCart) }
            ) {
                filter { eq("id", userId) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка удаления из корзины: ${e.message}")
        }
    }

    override suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId() ?: return onError("Пользователь не авторизован.")

            supabase.from("customers").update(
                { set("cart", emptyList<CartItem>()) }
            ) {
                filter { eq("id", userId) }
            }
            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка очистки корзины: ${e.message}")
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
