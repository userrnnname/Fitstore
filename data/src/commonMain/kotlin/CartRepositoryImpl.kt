package com.fitstore.data


import com.fitstore.data.domain.CartRepository
import com.fitstore.shared.domain.Cart
import com.fitstore.shared.domain.CartItem
import com.fitstore.shared.domain.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product,
)

class CartRepositoryImpl(
    private val supabase: SupabaseClient
) : CartRepository {

    override suspend fun getOrCreateCart(userId: String): Cart {
        val existing = supabase.from("carts")
            .select { filter { eq("user_id", userId) } }
            .decodeSingleOrNull<Cart>()
        existing?.let { return it }

        return existing ?: run {
            val newCart = Cart(
                id = null,
                userId = userId,
                createdAt = null,
                updatedAt = null
            )
            val inserted = supabase.from("carts").insert(newCart) {
                select()
            }.decodeSingle<Cart>()

            requireNotNull(inserted.id) { "Не удалось получить ID новой корзины" }
            return inserted
        }
    }

    override fun getCartItemsWithProductsFlow(userId: String): Flow<List<CartItemWithProduct>> = callbackFlow {
        var cart: Cart? = null
        try {
            cart = getOrCreateCart(userId)
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }
        val cartId = requireNotNull(cart.id) { "ID корзины равен null" }
        val channel = supabase.channel("cart-items-$cartId-${Clock.System.now()}")
        val nonNullCartId = requireNotNull(cart.id) { "ID корзины не может быть null" }

        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "cart_items"
            filter("cart_id", FilterOperator.EQ, nonNullCartId)
        }

        val job = launch {
            changes.collect {
                val fullList = fetchFullCartItems(cart.id)
                trySend(fullList.map { cartItemResp ->
                    CartItemWithProduct(
                        cartItem = cartItemResp.toEntity(),
                        product = cartItemResp.product
                            ?: error("Товар с таким ID ${cartItemResp.product_id} не найден.")
                    )
                })
            }
        }

        launch {
            channel.subscribe()
        }

        val initialList = fetchFullCartItems(cart.id)
        trySend(initialList.map { cartItemResp ->
            CartItemWithProduct(
                cartItem = cartItemResp.toEntity(),
                product = cartItemResp.product ?: error("Товар с таким ID ${cartItemResp.product_id} не найден.")
            )
        })

        awaitClose {
            job.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    private suspend fun fetchFullCartItems(cartId: String?): List<CartItemWithProductResponse> {
        val nonNullId = cartId ?: return emptyList()

        return supabase.from("cart_items")
            .select(columns = Columns.raw("id, cart_id, product_id, flavor, quantity, added_at, product:products(*)")) {
                filter {
                    eq("cart_id", nonNullId)
                }
            }
            .decodeList<CartItemWithProductResponse>()
    }
    override suspend fun addOrUpdateItem(cartId: String, productId: String, flavor: String?, quantity: Int) {
        val existingItem = supabase.from("cart_items")
            .select {
                filter {
                    eq("cart_id", cartId)
                    eq("product_id", productId)
                    if (flavor == null) {
                        exact("flavor", null)
                    } else {
                        eq("flavor", flavor)
                    }
                }
            }
            .decodeSingleOrNull<CartItem>()

        if (existingItem != null) {
            updateItemQuantity(existingItem.id!!, existingItem.quantity + quantity)
        } else {
            val newItem = CartItem(
                id = null,
                cartId = cartId,
                productId = productId,
                flavor = flavor,
                quantity = quantity,
                addedAt = null
            )
            supabase.from("cart_items").insert(newItem)
        }
    }

    override suspend fun updateItemQuantity(cartItemId: String, newQuantity: Int) {
        supabase.from("cart_items")
            .update({ set("quantity", newQuantity) }) {
                filter { eq("id", cartItemId) }
            }
    }

    override suspend fun removeItem(cartItemId: String) {
        supabase.from("cart_items")
            .delete {
                filter { eq("id", cartItemId) }
            }
    }

    override suspend fun clearCart(cartId: String?) {
        val safeId = cartId ?: return
        supabase.from("cart_items")
            .delete {
                filter { eq("cart_id", safeId) }
            }
    }
}

@Serializable
data class CartItemWithProductResponse(
    val id: String,
    val cart_id: String,
    val product_id: String,
    val flavor: String? = null,
    val quantity: Int,
    val added_at: String,
    val product: Product? = null
) {
    fun toEntity(): CartItem = CartItem(id, cart_id, product_id, flavor, quantity, added_at)
}
