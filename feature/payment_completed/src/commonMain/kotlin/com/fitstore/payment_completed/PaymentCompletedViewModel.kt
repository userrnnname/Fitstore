package com.fitstore.payment_completed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.fitstore.shared.navigation.Screen
import com.fitstore.shared.util.RequestState

class PaymentCompletedViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var screenState: RequestState<Unit> by mutableStateOf(RequestState.Loading)
        private set

    init {
        val args = savedStateHandle.toRoute<Screen.PaymentCompleted>()
        println("DEBUG: Payment result is ${args.isSuccess}")
        if (args.isSuccess == true) {
            screenState = RequestState.Success(Unit)
        } else {
            screenState = RequestState.Error(args.error ?: "Ошибка оплаты")
        }
    }
}