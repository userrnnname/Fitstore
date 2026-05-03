package com.fitstore.data

import com.fitstore.data.domain.CartRepository
import com.fitstore.data.domain.OrderRepository
import com.fitstore.shared.domain.Order
import com.fitstore.shared.domain.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderRepositoryImpl(
    private val supabase: SupabaseClient,
    private val cartRepository: CartRepository
) : OrderRepository {

    override suspend fun createOrderFromCart(
        userId: String,
        deliveryAddress: String,
        phoneNumber: String
    ): Result<Order> {
        return try {
            val cart = cartRepository.getOrCreateCart(userId)

            val itemsWithProduct = supabase.from("cart_items")
                .select(columns = Columns.raw("id, cart_id, product_id, flavor, quantity, added_at, product:products(*)")) {
                    filter { eq("cart_id", requireNotNull(cart.id)) }
                }
                .decodeList<CartItemWithProductResponse>()

            if (itemsWithProduct.isEmpty()) {
                return Result.failure(Exception("Корзина пуста"))
            }


            var totalAmount = 0.0
            val orderItems = itemsWithProduct.map { itemResp ->
                val priceAtMoment = itemResp.product?.price ?: 0.0
                totalAmount += priceAtMoment * itemResp.quantity
                OrderItem(
                    id = null,
                    orderId = "",
                    productId = itemResp.product_id,
                    productTitle = itemResp.product?.title ?: "",
                    productThumbnail = itemResp.product?.thumbnail ?: "",
                    flavor = itemResp.flavor,
                    quantity = itemResp.quantity,
                    priceAtMoment = priceAtMoment
                )
            }

            val newOrder = Order(
                id = null,
                customerId = userId,
                totalAmount = totalAmount,
                createdAt = null,
                deliveryAddress = deliveryAddress,
                phoneNumber = phoneNumber,
                status = "pending"
            )

            val insertedOrder = supabase.from("orders").insert(newOrder) {
                select()
            }.decodeSingle<Order>()

            val orderItemsToInsert = orderItems.map { it.copy(orderId = insertedOrder.id!!) }
            supabase.from("order_items").insert(orderItemsToInsert)

            cartRepository.clearCart(cart.id)

            Result.success(insertedOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastPurchasedProducts(userId: String, limit: Int): List<Product> {
        return supabase.from("user_purchased_products")
            .select(columns = Columns.raw("product_id, title, description, thumbnail, price, weight")) {
                filter { eq("customer_id", userId) }
                limit(3)
            }
            .decodeList<PurchasedProduct>()
            .map { it.toProduct() }
    }
}

@Serializable
data class Order(
    val id: String? = null,
    @SerialName("customer_id") val customerId: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val status: String = "pending"
)

@Serializable
data class OrderItem(
    val id: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_title") val productTitle: String,
    @SerialName("product_thumbnail") val productThumbnail: String,
    val flavor: String? = null,
    val quantity: Int,
    @SerialName("price_at_moment") val priceAtMoment: Double
)

@Serializable
data class PurchasedProduct(
    val product_id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val price: Double,
    val weight: Int? = null
) {
    fun toProduct() = Product(
        id = product_id,
        title = title,
        description = description,
        thumbnail = thumbnail,
        price = price,
        weight = weight,
        createdAt = 0L,
        category = "",
        flavors = null,
        isPopular = false,
        isDiscounted = false,
        isNew = false
    )
}