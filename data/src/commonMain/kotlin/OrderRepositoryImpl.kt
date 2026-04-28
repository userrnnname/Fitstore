package com.fitstore.data

import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.domain.Order
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class OrderRepositoryImpl(
    private val supabase: SupabaseClient,
    private val customerRepository: CustomerRepository,
) : OrderRepository {

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        try {
            val userId = getCurrentUserId()
            if (userId != null) {
                supabase.from("orders").insert(order)

                customerRepository.deleteAllCartItems(
                    onSuccess = {
                        onSuccess()
                    },
                    onError = { message ->
                        onError("Заказ создан, но корзина не очищена: $message")
                    }
                )
            } else {
                onError("Пользователь не авторизован.")
            }
        } catch (e: Exception) {
            onError("Ошибка при оформлении заказа: ${e.message}")
        }
    }
}