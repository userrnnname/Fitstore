package com.fitstore.payment_completed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.SupplementRepository
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.navigation.Screen
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PaymentCompletedViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val supplementRepository: SupplementRepository,
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    var screenState: RequestState<Unit> by mutableStateOf(RequestState.Loading)
        private set

    init {
        val args = savedStateHandle.toRoute<Screen.PaymentCompleted>()
        if (args.isSuccess == true) {
            finalizeOrder()
        } else {
            screenState = RequestState.Error(args.error ?: "Ошибка оплаты")
        }
    }

    private fun finalizeOrder() {
        viewModelScope.launch {
            val userId = customerRepository.getCurrentUserId()
            if (userId == null) {
                screenState = RequestState.Error("Пользователь не авторизован")
                return@launch
            }
            val cartItems = cartRepository.getCartItemsWithProductsFlow(userId).first()
            if (cartItems.isEmpty()) {
                screenState = RequestState.Success(Unit)
                return@launch
            }
            val customerResult = customerRepository.getCurrentCustomer()
            if (customerResult.isError()) {
                screenState = RequestState.Error("Не удалось загрузить профиль: ${customerResult.getErrorMessage()}")
                return@launch
            }
            val customer = customerResult.getSuccessData()
            val address = listOfNotNull(customer.city, customer.address).joinToString(", ")
            val phone = customer.phoneNumber?.number ?: ""
            if (address.isBlank() || phone.isBlank()) {
                screenState = RequestState.Error("Укажите адрес и телефон в профиле")
                return@launch
            }
            val result = orderRepository.createOrderFromCart(userId, address, phone)
            if (result.isSuccess) {
                createTracksForPurchasedItems(userId)
                screenState = RequestState.Success(Unit)
            } else {
                screenState = RequestState.Error(result.exceptionOrNull()?.message ?: "Ошибка оформления заказа")
            }
        }
    }

    private suspend fun createTracksForPurchasedItems(userId: String) {
        val cartItems = cartRepository.getCartItemsWithProductsFlow(userId).first()

        cartItems.forEach { item ->
            val product = item.product
            if (product.servings != null && product.servings!! > 0) {
                val newTrack = SupplementTrack(
                    customerId = userId,
                    productId = product.id ?: "",
                    productTitle = product.title,
                    productThumbnail = product.thumbnail,
                    totalServings = product.servings!!,
                    remainingServings = product.servings!!,
                    lastTakenDate = null
                )
                supplementRepository.addSupplementTrack(newTrack, {}, {})
            }
        }
    }
}