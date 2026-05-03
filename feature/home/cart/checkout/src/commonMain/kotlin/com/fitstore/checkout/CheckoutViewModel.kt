package com.fitstore.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.data.domain.PaymentRepository
import com.fitstore.data.domain.SupplementRepository
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.domain.PaymentItem
import com.fitstore.shared.domain.PhoneNumber
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
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
    val phoneNumber: PhoneNumber? = null
)

class CheckoutViewModel(
    private val paymentRepository: PaymentRepository,
    private val customerRepository: CustomerRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val supplementRepository: SupplementRepository,
    private val paymentLauncher: PaymentLauncher?
) : ViewModel() {

    var isPaymentLoading by mutableStateOf(false)
        private set

    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState by mutableStateOf(CheckoutScreenState())
        private set

    private val userId = customerRepository.getCurrentUserId()

    private val cartItemsFlow = userId?.let { userId ->
        cartRepository.getCartItemsWithProductsFlow(userId)
    } ?: flowOf(emptyList())

    val cartItemsState = cartItemsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalAmount = cartItemsFlow.map { items ->
        items.sumOf { it.product.price * it.cartItem.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    init {
        loadCustomerData()
    }

    private fun loadCustomerData() {
        viewModelScope.launch {
            customerRepository.readCustomerFlow().collect { customerResult ->
                if (customerResult.isSuccess()) {
                    val c = customerResult.getSuccessData()
                    screenState = screenState.copy(
                        id = c.id!!,
                        firstName = c.firstName,
                        lastName = c.lastName,
                        email = c.email,
                        city = c.city,
                        postalCode = c.postalCode,
                        address = c.address,
                        phoneNumber = c.phoneNumber
                    )
                    screenReady = RequestState.Success(Unit)
                } else if (customerResult.isError()) {
                    screenReady = RequestState.Error(customerResult.getErrorMessage())
                }
            }
        }
    }

    val isFormValid: Boolean
        get() = with(screenState) {
            lastName.length in 3..50 &&
                    firstName.length in 3..50 &&
                    !city.isNullOrBlank() && city.length in 3..50 &&
                    !address.isNullOrBlank() && address.length in 3..50 &&
                    postalCode != null && postalCode.toString().length in 4..10 &&
                    phoneNumber?.number?.length == 10
        }

    fun startOnlinePayment(
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val amount = totalAmount.value
        val orderId = "order_${Clock.System.now().toEpochMilliseconds()}"

        val paymentItems = cartItemsState.value.map { item ->
            PaymentItem(
                title = item.product.title,
                price = item.product.price,
                quantity = item.cartItem.quantity
            )
        }

        viewModelScope.launch {
            isPaymentLoading = true

            val result = paymentRepository.preparePayment(amount, orderId, paymentItems)

            result.onSuccess { paymentUrl ->
                paymentLauncher?.launchPayment(
                    amount = amount,
                    orderId = orderId,
                    paymentUrl = paymentUrl,
                    onSuccess = { saveOrderAfterPayment(amount, onSuccess, onError) },
                    onError = { message -> isPaymentLoading = false
                        onError(message) }
                )
            }.onFailure { error ->
                isPaymentLoading = false
                onError("Ошибка подготовки платежа: ${error.message}")
            }
        }
    }

    private fun saveOrderAfterPayment(
        amount: Double,
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        updateCustomer(
            onSuccess = {
                createOrder(
                    onSuccess = {
                        isPaymentLoading = false
                        onSuccess(amount)
                    },
                    onError = { error ->
                        isPaymentLoading = false
                        onError("Оплата прошла, но заказ не сохранен: $error")
                    }
                )
            },
            onError = { error ->
                isPaymentLoading = false
                onError("Ошибка обновления данных: $error")
            }
        )
    }

    fun payOnDelivery(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCustomer(
            onSuccess = {
                createOrder(
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    private fun updateCustomer(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                customer = Customer(
                    id = screenState.id,
                    lastName = screenState.lastName,
                    firstName = screenState.firstName,
                    email = screenState.email,
                    city = screenState.city,
                    postalCode = screenState.postalCode,
                    address = screenState.address,
                    phoneNumber = screenState.phoneNumber,
                    isAdmin = false
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    private fun createOrder(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val userId = customerRepository.getCurrentUserId()
            if (userId == null) {
                onError("Пользователь не авторизован")
                return@launch
            }
            val currentCartItems = cartItemsFlow.first()
            val deliveryAddress = "${screenState.city}, ${screenState.address}"
            val phoneNumber = screenState.phoneNumber?.number ?: ""
            val result = orderRepository.createOrderFromCart(
                userId = userId,
                deliveryAddress = deliveryAddress,
                phoneNumber = phoneNumber
            )
            result.onSuccess {
                currentCartItems.forEach { item ->
                    val product = item.product
                    if (product.servings != null && product.servings!! > 0) {
                        repeat(item.cartItem.quantity) {
                            supplementRepository.addSupplementTrack(
                                track = SupplementTrack(
                                    customerId = userId,
                                    productId = product.id ?: "",
                                    productTitle = product.title,
                                    productThumbnail = product.thumbnail,
                                    totalServings = product.servings!!,
                                    remainingServings = product.servings!!,
                                    lastTakenDate = null
                                ),
                                onSuccess = {},
                                onError = { error -> println("Ошибка создания трека: $error") }
                            )
                        }
                    }
                }
                onSuccess()
            }.onFailure { e ->
                onError(e.message ?: "Ошибка оформления заказа")
            }
        }
    }

    fun updateFirstName(v: String) { screenState = screenState.copy(firstName = v) }
    fun updateLastName(v: String) { screenState = screenState.copy(lastName = v) }
    fun updateCity(v: String) { screenState = screenState.copy(city = v) }
    fun updateAddress(v: String) { screenState = screenState.copy(address = v) }
    fun updatePostalCode(v: Int?) { screenState = screenState.copy(postalCode = v) }
    fun updatePhoneNumber(v: String) {
        val currentDialCode = screenState.phoneNumber?.dialCode ?: 7
        screenState = screenState.copy(
            phoneNumber = PhoneNumber(currentDialCode, v)
        )
    }
}