package com.fitstore.data

import com.fitstore.data.domain.PaymentRepository
import com.fitstore.shared.domain.PaymentItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

@Serializable
data class PaymentResponse(val url: String? = null)

class PaymentRepositoryImpl(
    private val supabase: SupabaseClient
) : PaymentRepository {

    override suspend fun preparePayment(
        amount: Double,
        orderId: String,
        items: List<PaymentItem>
    ): Result<String> {
        return runCatching {
            val response = supabase.functions.invoke(
                function = "create-yandex-payment",
                body = buildJsonObject {
                    put("amount", amount)
                    put("orderId", orderId)
                    put("items", Json.encodeToJsonElement(items))
                }
            )
            val paymentData = response.body<PaymentResponse>()
            paymentData.url ?: throw Exception("URL не найден в ответе")
        }
    }
}