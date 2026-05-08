package com.fitstore.shared.payment

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PaymentLauncher actual constructor(
    private val clientApplicationKey: String,
    private val shopId: String
) {
    actual fun launchPayment(
        amount: String,
        orderId: String,
        description: String,
        onResult: (PaymentResult) -> Unit
    ) {
        println("iOS: оплата заказа $orderId на сумму $amount руб.")
        // Имитируем успех
        onResult(PaymentResult.Success("mock_token_$orderId"))
    }
}