package com.fitstore.shared.domain

import androidx.compose.ui.graphics.Color
import com.fitstore.shared.CategoryBlue
import com.fitstore.shared.CategoryGreen
import com.fitstore.shared.CategoryPurple
import com.fitstore.shared.CategoryRed
import com.fitstore.shared.CategoryYellow
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class Product(
    val id: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: List<String>? = null,
    val weight: Int? = null,
    val price: Double,
    val isPopular: Boolean = false,
    val isDiscounted: Boolean = false,
    val isNew: Boolean = false
)

enum class ProductCategory(
    val title: String,
    val color: Color
) {
    Protein(
        title = "Протеин",
        color = CategoryYellow
    ),
    Creatine(
        title = "Креатин",
        color = CategoryBlue
    ),
    PreWorkout(
        title = "Перед тренировкой",
        color = CategoryGreen
    ),
    Gainers(
        title = "Гейнеры",
        color = CategoryPurple
    ),
    Accessories(
        title = "Аксессуары",
        color = CategoryRed
    )
}
