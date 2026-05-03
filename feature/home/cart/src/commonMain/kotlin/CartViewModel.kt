package com.fitstore.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.CartItemWithProduct
import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    private val userId: String? = customerRepository.getCurrentUserId()

    val cartItemsWithProducts: StateFlow<RequestState<List<CartItemWithProduct>>> =
        if (userId != null) {
            cartRepository.getCartItemsWithProductsFlow(userId)
                .map { RequestState.Success(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = RequestState.Loading
                )
        } else {
            MutableStateFlow(RequestState.Error("Пользователь не авторизован")).asStateFlow()
        }

    val totalAmount = cartItemsWithProducts.map { state ->
        if (state is RequestState.Success) {
            state.data.sumOf { it.product.price * it.cartItem.quantity }
        } else 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun updateCartItemQuantity(
        cartItemId: String,
        newQuantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartRepository.removeItem(cartItemId)
                } else {
                    cartRepository.updateItemQuantity(cartItemId, newQuantity)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Не удалось обновить количество")
            }
        }
    }

    fun deleteCartItem(
        cartItemId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                cartRepository.removeItem(cartItemId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Не удалось удалить товар")
            }
        }
    }
}