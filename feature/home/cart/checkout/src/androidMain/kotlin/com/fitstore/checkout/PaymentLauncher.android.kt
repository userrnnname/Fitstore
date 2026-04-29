package com.fitstore.checkout

import androidx.activity.ComponentActivity
import com.yandex.pay.MerchantData
import com.yandex.pay.MerchantId
import com.yandex.pay.MerchantName
import com.yandex.pay.MerchantUrl
import com.yandex.pay.PaymentData
import com.yandex.pay.PaymentSession
import com.yandex.pay.YPay
import com.yandex.pay.YPayApiEnvironment
import com.yandex.pay.YPayConfig
import com.yandex.pay.YPayContractParams
import com.yandex.pay.YPayLauncher
import com.yandex.pay.YPayResult

actual class PaymentLauncher(
    private val activity: ComponentActivity
) {
    private lateinit var launcher: YPayLauncher
    private lateinit var paymentSession: PaymentSession
    private var onSuccessCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    init {
        val yPayConfig = YPayConfig(
            merchantData = MerchantData(
                id = MerchantId("5d4a2d9d-2d4b-4e1c-8e3b-9d6f7a8b9c0d"),
                name = MerchantName("Fitstore"),
                url = MerchantUrl("https://sandbox.example.merchant.ru")
            ),
            environment = YPayApiEnvironment.SANDBOX
        )


        paymentSession = YPay.getYandexPaymentSession(
            context = activity,
            config = yPayConfig
        )

        launcher = YPayLauncher(activity) { result: YPayResult ->
            when (result) {
                is YPayResult.Success -> {
                    onSuccessCallback?.invoke()
                }

                is YPayResult.Cancelled -> {
                    onErrorCallback?.invoke("Платёж отменён")
                }

                is YPayResult.Failure -> {
                    val message = result.errorMsg
                    onErrorCallback?.invoke(message)
                }
            }
            clearCallbacks()
        }
    }

    actual fun initialize() {}
    private fun clearCallbacks() {
        onSuccessCallback = null
        onErrorCallback = null
    }

    actual fun launchPayment(
        amount: Double,
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        this.onSuccessCallback = onSuccess
        this.onErrorCallback = onError

        val paymentData = PaymentData.PaymentUrlFlowData(
            paymentUrl = "https://demo.yandex.ru/payment?orderId=$orderId&amount=$amount"
        )
        val params = YPayContractParams(
            paymentSession = paymentSession,
            paymentData = paymentData
        )
        launcher.launch(params)
    }
}