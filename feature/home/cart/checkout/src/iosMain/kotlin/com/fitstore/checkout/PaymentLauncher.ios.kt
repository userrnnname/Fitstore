package com.fitstore.checkout

import platform.UIKit.UIViewController

actual class PaymentLauncher(
    private val viewController: UIViewController
) {
    actual fun launchPayment(
        amount: Double,
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onError("Оплата онлайн для iOS пока не настроена. Пожалуйста, используйте оплату при доставке.")
    }
}