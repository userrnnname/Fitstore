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
import com.fitstore.shared.PaymentType
import com.fitstore.shared.domain.Customer
import com.fitstore.shared.domain.PhoneNumber
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.payment.PaymentLauncher
import com.fitstore.shared.payment.PaymentResult
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
    private val paymentLauncher: PaymentLauncher
) : ViewModel() {

    var currentPaymentType by mutableStateOf(PaymentType.NONE)
        private set

    val isPaymentLoading get() = currentPaymentType != PaymentType.NONE

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

    fun startOnlinePayment(onSuccess: (Double) -> Unit, onError: (String) -> Unit) {
        val amount = totalAmount.value
        val orderId = "order_${Clock.System.now().toEpochMilliseconds()}"

        paymentLauncher.launchPayment(
            amount = amount.toString(),
            orderId = orderId,
            description = "Оплата заказа №$orderId в Fitstore"
        ) { result ->
            when (result) {
                is PaymentResult.Success -> {
                    viewModelScope.launch {
                        currentPaymentType = PaymentType.ONLINE
                        paymentRepository.confirmPayment(orderId, result.paymentToken, amount)
                            .onSuccess {
                                processOrderFullCycle(
                                    amount = amount,
                                    method = "online",
                                    onSuccess = {
                                        currentPaymentType = PaymentType.NONE
                                        onSuccess(amount)
                                    },
                                    onError = { errorMsg ->
                                        currentPaymentType = PaymentType.NONE
                                        onError(errorMsg)
                                    }
                                )
                            }
                            .onFailure { error ->
                                currentPaymentType = PaymentType.NONE
                                onError("Ошибка подтверждения платежа: ${error.message}")
                            }
                    }
                }
                is PaymentResult.Error -> {
                    onError(result.message)
                }
                PaymentResult.Cancelled -> {
                    onError("Оплата отменена пользователем")
                }
            }
        }
    }

    fun payOnDelivery(onSuccess: (Double) -> Unit, onError: (String) -> Unit) {
        val amountAtClick = totalAmount.value
        viewModelScope.launch {
            currentPaymentType = PaymentType.DELIVERY
            processOrderFullCycle(
                amount = amountAtClick,
                method = "delivery",
                onSuccess = {
                    currentPaymentType = PaymentType.NONE
                    onSuccess(amountAtClick)
                },
                onError = {
                    currentPaymentType = PaymentType.NONE
                    onError(it)
                }
            )
        }
    }

    private suspend fun processOrderFullCycle(
        amount: Double,
        method: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            updateCustomerSuspend()
            createOrderSuspend()
            viewModelScope.launch {
                paymentRepository.sendEmailReceipt(
                    orderId = "ID_${Clock.System.now().toEpochMilliseconds()}",
                    email = screenState.email,
                    amount = amount,
                    method = method
                )
            }
            currentPaymentType = PaymentType.NONE
            onSuccess()
        } catch (e: Exception) {
            currentPaymentType = PaymentType.NONE
            onError(e.message ?: "Непредвиденная ошибка")
        }
    }

    private suspend fun updateCustomerSuspend() {
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
            onSuccess = {},
            onError = { throw Exception(it) }
        )
    }

    private suspend fun createOrderSuspend() {
        val userId = customerRepository.getCurrentUserId() ?: throw Exception("Пользователь не авторизован")
        val currentCartItems = cartItemsFlow.first()
        val deliveryAddress = "${screenState.city}, ${screenState.address}"
        val phone = screenState.phoneNumber?.number ?: ""

        val result = orderRepository.createOrderFromCart(userId, deliveryAddress, phone)

        if (result.isSuccess) {
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
                                lastTakenDate = null,
                            ),
                            onSuccess = {},
                            onError = { println("Ошибка трека: $it") }
                        )
                    }
                }
            }
        } else {
            throw Exception(result.exceptionOrNull()?.message ?: "Ошибка базы данных")
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