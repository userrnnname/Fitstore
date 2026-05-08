package com.fitstore.data

import com.fitstore.data.domain.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.headers
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PaymentRepositoryImpl(
    private val supabase: SupabaseClient
) : PaymentRepository {

    override suspend fun confirmPayment(
        orderId: String,
        paymentToken: String,
        amount: Double
    ): Result<Unit> = runCatching {
        supabase.functions.invoke(
            "confirm-yookassa-payment",
            body = buildJsonObject {
                put("orderId", orderId)
                put("paymentToken", paymentToken)
                put("amount", amount)
            }
        )
    }

    @OptIn(InternalAPI::class)
    override suspend fun sendEmailReceipt(
        orderId: String,
        email: String,
        amount: Double,
        method: String) {
        runCatching {
            val jsonString = buildJsonObject {
                put("email", email)
                put("orderId", orderId)
                put("amount", amount)
                put("paymentMethod", method)
            }.toString()

            supabase.functions.invoke("send-receipt") {
                headers { append("Content-Type", "application/json") }
                body = jsonString
            }
        }.onFailure {
            println("ERROR: Failed to send email: ${it.message}")
        }
    }
}