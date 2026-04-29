package com.fitstore.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.ProductRepository
import com.fitstore.shared.domain.CartItem
import com.fitstore.shared.domain.Product
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.collections.emptyList

class CartViewModel(
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _cartItemsWithProducts = MutableStateFlow<RequestState<List<Pair<CartItem, Product>>>>(RequestState.Loading)
    val cartItemsWithProducts = _cartItemsWithProducts.asStateFlow()

    val totalAmountFlow = _cartItemsWithProducts
        .map { state ->
            if (state is RequestState.Success) {
                val total = state.data.sumOf { (cartItem, product) -> product.price * cartItem.quantity }
                RequestState.Success(total)
            } else {
                state as RequestState<Double>
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RequestState.Loading)

    init {
        readCustomerFlow()
    }

    private fun readCustomerFlow() {
        viewModelScope.launch {
            val customerFlow = customerRepository.readCustomerFlow()

            @OptIn(ExperimentalCoroutinesApi::class)
            val productsFlow = customerFlow
                .filter { it.isSuccess() }
                .map { it.getSuccessData().cart.map { item -> item.productId } }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isNotEmpty()) productRepository.readProductsByIdsFlow(ids)
                    else flowOf(RequestState.Success(emptyList()))
                }

            combine(customerFlow, productsFlow) { customerState, productsState ->
                when {
                    customerState.isSuccess() && productsState.isSuccess() -> {
                        val cart = customerState.getSuccessData().cart
                        val products = productsState.getSuccessData()
                        val result = cart.mapNotNull { cartItem ->
                            val product = products.find { it.id == cartItem.productId }
                            product?.let { cartItem to it }
                        }
                        RequestState.Success(result)
                    }
                    customerState.isError() -> RequestState.Error(customerState.getErrorMessage())
                    productsState.isError() -> RequestState.Error(productsState.getErrorMessage())
                    else -> RequestState.Loading
                }
            }.collect { result ->
                _cartItemsWithProducts.value = result
            }
        }
    }

    fun updateCartItemQuantity(id: String, quantity: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val oldState = _cartItemsWithProducts.value

            if (oldState is RequestState.Success) {
                val updatedList = oldState.data.map { (item, product) ->
                    if ((item.id ?: "") == id) item.copy(quantity = quantity) to product
                    else item to product
                }
                _cartItemsWithProducts.value = RequestState.Success(updatedList)
            }

            customerRepository.updateCartItemQuantity(
                id = id,
                quantity = quantity,
                onSuccess = { onSuccess() },
                onError = { error ->

                    _cartItemsWithProducts.value = oldState
                    onError(error)
                }
            )
        }
    }

    fun deleteCartItem(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val oldState = _cartItemsWithProducts.value

            if (oldState is RequestState.Success) {
                val updatedList = oldState.data.filterNot { it.first.id == id }
                _cartItemsWithProducts.value = RequestState.Success(updatedList)
            }

            customerRepository.deleteCartItem(
                id = id,
                onSuccess = { onSuccess() },
                onError = { error ->
                    _cartItemsWithProducts.value = oldState
                    onError(error)
                }
            )
        }
    }
}