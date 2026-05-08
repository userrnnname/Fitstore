package com.fitstore.shared.payment

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PaymentLauncher(
    clientApplicationKey: String,
    shopId: String
) {
    fun launchPayment(
        amount: String,
        orderId: String,
        description: String,
        onResult: (PaymentResult) -> Unit
    )
}