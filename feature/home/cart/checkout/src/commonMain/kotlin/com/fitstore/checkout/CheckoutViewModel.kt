package com.fitstore.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.ProductRepository
import com.fitstore.shared.domain.CartItem
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.domain.Order
import com.fitstore.shared.domain.PhoneNumber
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class CheckoutScreenState(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String? = null,
    val postalCode: Int? = null,
    val address: String? = null,
    val phoneNumber: PhoneNumber? = null,
    val cart: List<CartItem> = emptyList(),
)
class CheckoutViewModel(
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val paymentLauncher: PaymentLauncher?
) : ViewModel() {

    var isPaymentLoading by mutableStateOf(false)
        private set

    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState by mutableStateOf(CheckoutScreenState())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalAmount: StateFlow<Double> = snapshotFlow { screenState.cart }
        .flatMapLatest { cart ->
            if (cart.isEmpty()) flowOf(0.0)
            else productRepository.readProductsByIdsFlow(cart.map { it.productId })
                .map { result ->
                    val products = result.getSuccessDataOrNull() ?: emptyList()
                    cart.sumOf { item ->
                        val price = products.find { it.id == item.productId }?.price ?: 0.0
                        price * item.quantity
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            customerRepository.readCustomerFlow().collect { customerResult ->
                if (customerResult.isSuccess()) {
                    val c = customerResult.getSuccessData()

                    screenState = screenState.copy(
                        id = c.id, firstName = c.firstName, lastName = c.lastName,
                        email = c.email, city = c.city, postalCode = c.postalCode,
                        address = c.address, phoneNumber = c.phoneNumber, cart = c.cart
                    )
                    screenReady = RequestState.Success(Unit)
                } else if (customerResult.isError()) {
                    screenReady = RequestState.Error(customerResult.getErrorMessage())
                }
            }
        }
    }

    fun startOnlinePayment(
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val amount = totalAmount.value
        val orderId = "order_${
            
            Clock.System.now().toEpochMilliseconds()}"

        isPaymentLoading = true

        paymentLauncher?.launchPayment(
            amount = amount,
            orderId = orderId,
            onSuccess = {
                isPaymentLoading = false
                onSuccess(amount)
            },
            onError = { error ->
                isPaymentLoading = false
                onError(error)
            }
        )
    }

    val isFormValid: Boolean
        get() = with(screenState) {
            lastName.length in 3..50 &&
                    firstName.length in 3..50 &&
                    !city.isNullOrBlank() && city.length in 3..50 &&
                    !address.isNullOrBlank() && address.length in 3..50 &&
                    postalCode != null && postalCode.toString().length in 4..10 &&
                    phoneNumber?.number?.length == 10 &&
                    cart.isNotEmpty()
        }

    fun updateFirstName(v: String) { screenState = screenState.copy(firstName = v) }
    fun updateLastName(v: String) { screenState = screenState.copy(lastName = v) }
    fun updateCity(v: String) { screenState = screenState.copy(city = v) }
    fun updateAddress(v: String) { screenState = screenState.copy(address = v) }
    fun updatePostalCode(v: Int?) { screenState = screenState.copy(postalCode = v) }
    fun updatePhoneNumber(v: String) { screenState = screenState.copy(phoneNumber = PhoneNumber(7, v)) }

    fun payOnDelivery(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCustomer(
            onSuccess = {
                createTheOrder(
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    fun updateCustomer(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                customer = Customer(
                    screenState.id,
                    screenState.lastName,
                    screenState.firstName,
                    screenState.email,
                    screenState.city,
                    screenState.postalCode,
                    screenState.address,
                    screenState.phoneNumber
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    private fun createTheOrder(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val total = totalAmount.value

            orderRepository.createTheOrder(
                order = Order(
                    customerId = screenState.id,
                    items = screenState.cart,
                    totalAmount = total
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }
}