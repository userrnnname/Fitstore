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
    val id: String? = null,
    @SerialName("created_at") val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: List<String>? = null,
    val weight: Int? = null,
    val price: Double,
    val servings: Int? = null,
    @SerialName("is_popular") val isPopular: Boolean = false,
    @SerialName("is_discounted") val isDiscounted: Boolean = false,
    @SerialName("is_new") val isNew: Boolean = false
)

enum class ProductCategory(val title: String, val color: Color) {
    Protein("ПРОТЕИН", CategoryYellow),
    Creatine("КРЕАТИН", CategoryBlue),
    PreWorkout("ПЕРЕД ТРЕНИРОВКОЙ", CategoryGreen),
    Gainers("ГЕЙНЕРЫ", CategoryPurple),
    Accessories("АКСЕССУАРЫ", CategoryRed);

    companion object {
        fun fromString(value: String?): ProductCategory {
            return when (value?.lowercase()) {
                "protein" -> Protein
                "creatine" -> Creatine
                "preworkout" -> PreWorkout
                "gainers" -> Gainers
                "accessories" -> Accessories
                else -> Protein
            }
        }
    }
}
