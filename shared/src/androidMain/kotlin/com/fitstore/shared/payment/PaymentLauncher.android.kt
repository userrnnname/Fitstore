package com.fitstore.shared.payment

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import ru.yoomoney.sdk.kassa.payments.Checkout
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PaymentLauncher actual constructor(
    private val clientApplicationKey: String,
    private val shopId: String
) {
    private var activity: ComponentActivity? = null
    private var pendingResultCallback: ((PaymentResult) -> Unit)? = null

    fun attachActivity(activity: ComponentActivity) {
        this.activity = activity
    }

    actual fun launchPayment(
        amount: String,
        orderId: String,
        description: String,
        onResult: (PaymentResult) -> Unit
    ) {
        val act = activity ?: throw IllegalStateException("Activity not attached")
        pendingResultCallback = onResult

        val paymentParameters = PaymentParameters(
            amount = Amount(java.math.BigDecimal(amount), java.util.Currency.getInstance("RUB")),
            title = description,
            subtitle = "Заказ №$orderId",
            clientApplicationKey = clientApplicationKey,
            shopId = shopId,
            savePaymentMethod = SavePaymentMethod.OFF,
            paymentMethodTypes = setOf(PaymentMethodType.BANK_CARD)
        )

        val intent = Checkout.createTokenizeIntent(act, paymentParameters)
        act.startActivityForResult(intent, REQUEST_CODE_TOKENIZE)
    }

    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_TOKENIZE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val tokenResult = data?.let { Checkout.createTokenizationResult(it) }
                    val token = tokenResult?.paymentToken
                    if (token != null) {
                        pendingResultCallback?.invoke(PaymentResult.Success(token))
                    } else {
                        pendingResultCallback?.invoke(PaymentResult.Error("Не удалось получить токен"))
                    }
                }

                Activity.RESULT_CANCELED -> {
                    pendingResultCallback?.invoke(PaymentResult.Cancelled)
                }

                else -> {
                    pendingResultCallback?.invoke(PaymentResult.Error("Неизвестная ошибка"))
                }
            }
            pendingResultCallback = null
        }
    }

    companion object {
        private const val REQUEST_CODE_TOKENIZE = 1001
    }

}