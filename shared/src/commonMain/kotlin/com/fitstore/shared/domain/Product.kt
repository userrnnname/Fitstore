package com.fitstore.shared.domain

import androidx.compose.ui.graphics.Color
import com.fitstore.shared.CategoryBlue
import com.fitstore.shared.CategoryGreen
import com.fitstore.shared.CategoryPurple
import com.fitstore.shared.CategoryRed
import com.fitstore.shared.CategoryYellow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Product(
    val id: String,
    @SerialName("createdAt")
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: List<String>? = null,
    val weight: Int? = null,
    val price: Double,
    @SerialName("isPopular")
    val isPopular: Boolean = false,
    @SerialName("isDiscounted")
    val isDiscounted: Boolean = false,
    @SerialName("isNew")
    val isNew: Boolean = false
)

enum class ProductCategory(
    val title: String,
    val color: Color
) {
    Protein(
        title = "ПРОТЕИН",
        color = CategoryYellow
    ),
    Creatine(
        title = "КРЕАТИН",
        color = CategoryBlue
    ),
    PreWorkout(
        title = "ПЕРЕД ТРЕНИРОВКОЙ",
        color = CategoryGreen
    ),
    Gainers(
        title = "ГЕЙНЕРЫ",
        color = CategoryPurple
    ),
    Accessories(
        title = "АКСЕССУАРЫ",
        color = CategoryRed
    )
}
