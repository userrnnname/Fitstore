package com.fitstore.payment_completed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.shared.domain.Order
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.fitstore.shared.navigation.Screen
import androidx.navigation.toRoute

class PaymentCompletedViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    var screenState: RequestState<Unit> by mutableStateOf(RequestState.Loading)
        private set

    init {
        val args = savedStateHandle.toRoute<Screen.PaymentCompleted>()

        if (args.isSuccess == true) {
            val amount = args.totalAmount ?: 0.0
            finalizeOrder(amount)
        } else {
            screenState = RequestState.Error(args.error ?: "Ошибка оплаты")
        }
    }

    private fun finalizeOrder(amount: Double) {
        viewModelScope.launch {
            val customerResult = customerRepository.readCustomerFlow().first()
            if (customerResult.isSuccess()) {
                val customer = customerResult.getSuccessData()

                orderRepository.createTheOrder(
                    order = Order(
                        customerId = customer.id,
                        items = customer.cart,
                        totalAmount = amount
                    ),
                    onSuccess = {
                        viewModelScope.launch {
                            deleteAllCartItems()
                        }
                    },
                    onError = { screenState = RequestState.Error(it) }
                )
            } else {
                screenState = RequestState.Error("Не удалось получить данные профиля")
            }
        }
    }

    private suspend fun deleteAllCartItems() {
        customerRepository.deleteAllCartItems(
            onSuccess = { screenState = RequestState.Success(Unit) },
            onError = { screenState = RequestState.Error("Заказ создан, но корзина не очищена: $it") }
        )
    }
}