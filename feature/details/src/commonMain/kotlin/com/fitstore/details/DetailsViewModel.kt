package com.fitstore.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.ProductRepository
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val customerRepository: CustomerRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val product = productRepository.readProductByIdFlow(
        savedStateHandle.get<String>("id") ?: ""
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestState.Loading
    )

    var quantity by mutableStateOf(1)
        private set

    var selectedFlavor: String? by mutableStateOf(null)
        private set

    fun updateQuantity(value: Int) {
        quantity = value
    }

    fun updateFlavor(value: String) {
        selectedFlavor = value
    }

    fun addItemToCart(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val userId = customerRepository.getCurrentUserId()
            if (userId == null) {
                onError("Пользователь не авторизован.")
                return@launch
            }
            val productId = savedStateHandle.get<String>("id")
            if (productId == null) {
                onError("ID товара не найден.")
                return@launch
            }

            try {
                val cart = cartRepository.getOrCreateCart(userId)
                cartRepository.addOrUpdateItem(
                    cartId = cart.id!!,
                    productId = productId,
                    flavor = selectedFlavor,
                    quantity = quantity
                )
                onSuccess()
            } catch (e: Exception) {
                onError("Ошибка добавления в корзину: ${e.message}")
            }
        }
    }
}