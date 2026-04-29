package com.fitstore.checkout

expect class PaymentLauncher {
    fun initialize()
    fun launchPayment(
        amount: Double,
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}