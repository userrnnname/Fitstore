package com.fitstore.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentState {
    object Ready : PaymentState()
    object Loading : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class PaymentViewModel(
    private val paymentLauncher: PaymentLauncher
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Ready)
    val paymentState: StateFlow<PaymentState> = _paymentState

    fun startPayment(amount: Double, orderId: String) {
        _paymentState.value = PaymentState.Loading

        paymentLauncher.launchPayment(
            amount = amount,
            orderId = orderId,
            onSuccess = {
                _paymentState.value = PaymentState.Success
            },
            onError = { errorMessage ->
                _paymentState.value = PaymentState.Error(errorMessage)
            }
        )
    }

    fun resetState() {
        _paymentState.value = PaymentState.Ready
    }
}