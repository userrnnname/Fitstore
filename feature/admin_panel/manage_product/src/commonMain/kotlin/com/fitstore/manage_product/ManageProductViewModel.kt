package com.fitstore.manage_product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.AdminRepository
import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.util.RequestState
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
data class ManageProductState(
    val id: String = Uuid.random().toString(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val category: ProductCategory = ProductCategory.Protein,
    val flavors: String = "",
    val priceString: String = "",
    val weightString: String = "",
    val servingsString: String = "",
    val isNew: Boolean = false,
    val isPopular: Boolean = false,
    val isDiscounted: Boolean = false,
)

class ManageProductViewModel(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val productId = savedStateHandle.get<String>("id") ?: ""

    var screenState by mutableStateOf(ManageProductState())
        private set

    var thumbnailUploaderState: RequestState<Unit> by mutableStateOf(RequestState.Idle)
        private set

    val isFormValid: Boolean get() {
        val price = screenState.priceString.toDoubleOrNull()
        return screenState.title.isNotEmpty() &&
                screenState.description.isNotEmpty() &&
                screenState.thumbnail.isNotEmpty() &&
                price != null && price > 0
    }

    init {
        productId.takeIf { it.isNotEmpty() }?.let { id ->
            viewModelScope.launch {
                val selectedProduct = adminRepository.readProductById(id)
                if (selectedProduct.isSuccess()) {
                    val product = selectedProduct.getSuccessData()

                    screenState = screenState.copy(
                        id = product.id!!,
                        createdAt = product.createdAt,
                        title = product.title,
                        description = product.description,
                        thumbnail = product.thumbnail,
                        category = ProductCategory.valueOf(product.category),
                        flavors = product.flavors?.joinToString(",") ?: "",
                        priceString = if (product.price == 0.0) "" else product.price.toFormattedString(),
                        weightString = product.weight?.toString() ?: "",
                        servingsString = product.servings?.toString() ?: "",
                        isNew = product.isNew,
                        isPopular = product.isPopular,
                        isDiscounted = product.isDiscounted
                    )
                    updateThumbnailUploaderState(RequestState.Success(Unit))
                }
            }
        }
    }

    private fun Double.toFormattedString(): String {
        return if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()
    }

    fun updatePriceString(value: String) {
        val filtered = value.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && value.indexOf('.') == index)
        }
        screenState = screenState.copy(priceString = filtered)
    }
    fun updateWeightString(value: String) {
        val filtered = value.filter { it.isDigit() }
        screenState = screenState.copy(weightString = filtered)
    }
    fun updateServingsString(value: String) {
        screenState = screenState.copy(servingsString = value.filter { it.isDigit() })
    }
    fun updateTitle(value: String) { screenState = screenState.copy(title = value) }
    fun updateDescription(value: String) { screenState = screenState.copy(description = value) }
    fun updateThumbnail(value: String) { screenState = screenState.copy(thumbnail = value) }
    fun updateThumbnailUploaderState(value: RequestState<Unit>) { thumbnailUploaderState = value }
    fun updateCategory(value: ProductCategory) { screenState = screenState.copy(category = value) }
    fun updateFlavors(value: String) { screenState = screenState.copy(flavors = value) }
    fun updateNew(value: Boolean) { screenState = screenState.copy(isNew = value) }
    fun updatePopular(value: Boolean) { screenState = screenState.copy(isPopular = value) }
    fun updateDiscounted(value: Boolean) { screenState = screenState.copy(isDiscounted = value) }

    fun createNewProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            adminRepository.createNewProduct(
                product = buildProduct(),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun updateProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (isFormValid) {
            viewModelScope.launch {
                adminRepository.updateProduct(
                    product = buildProduct(),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } else {
            onError("Пожалуйста заполните информацию.")
        }
    }

    private fun buildProduct() = Product(
        id = screenState.id,
        createdAt = screenState.createdAt,
        title = screenState.title,
        description = screenState.description,
        thumbnail = screenState.thumbnail,
        category = screenState.category.name,
        flavors = screenState.flavors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        weight = screenState.weightString.toIntOrNull(),
        price = screenState.priceString.toDoubleOrNull() ?: 0.0,
        servings = screenState.servingsString.toIntOrNull(),
        isNew = screenState.isNew,
        isPopular = screenState.isPopular,
        isDiscounted = screenState.isDiscounted
    )

    fun uploadThumbnailToStorage(
        byteArray: ByteArray?,
        onSuccess: () -> Unit,
    ) {
        if (byteArray == null) {
            updateThumbnailUploaderState(RequestState.Error("Ошибка при выборе изображения."))
            return
        }

        updateThumbnailUploaderState(RequestState.Loading)

        viewModelScope.launch {
            try {
                val downloadUrl = adminRepository.uploadImageToStorage(byteArray)

                if (downloadUrl.isNullOrEmpty()) {
                    updateThumbnailUploaderState(
                        RequestState.Error("Не удалось загрузить фото. Возможно, оно слишком большое (макс. 5МБ).")
                    )
                    return@launch
                }

                productId.takeIf { it.isNotEmpty() }?.let { id ->
                    adminRepository.updateProductThumbnail(
                        productId = id,
                        downloadUrl = downloadUrl,
                        onSuccess = {
                            onSuccess()
                            updateThumbnailUploaderState(RequestState.Success(Unit))
                            updateThumbnail(downloadUrl)
                        },
                        onError = { message ->
                            updateThumbnailUploaderState(RequestState.Error(message))
                        }
                    )
                } ?: run {
                    onSuccess()
                    updateThumbnailUploaderState(RequestState.Success(Unit))
                    updateThumbnail(downloadUrl)
                }
            } catch (e: Exception) {
                updateThumbnailUploaderState(RequestState.Error("Ошибка при загрузке: ${e.message}"))
            }
        }
    }

    fun deleteThumbnailFromStorage(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (screenState.thumbnail.isEmpty()) return

        viewModelScope.launch {
            adminRepository.deleteImageFromStorage(
                downloadUrl = screenState.thumbnail,
                onSuccess = {
                    productId.takeIf { it.isNotEmpty() }?.let { id ->
                        viewModelScope.launch {
                            adminRepository.updateProductThumbnail(
                                productId = id,
                                downloadUrl = "",
                                onSuccess = {
                                    updateThumbnail("")
                                    updateThumbnailUploaderState(RequestState.Idle)
                                    onSuccess()
                                },
                                onError = { message -> onError(message) }
                            )
                        }
                    } ?: run {
                        updateThumbnail("")
                        updateThumbnailUploaderState(RequestState.Idle)
                        onSuccess()
                    }
                },
                onError = onError
            )
        }
    }
    fun deleteProduct(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        productId.takeIf { it.isNotEmpty() }?.let { id ->
            viewModelScope.launch {
                adminRepository.deleteProduct(
                    productId = id,
                    onSuccess = {
                        deleteThumbnailFromStorage(
                            onSuccess = {},
                            onError = {}
                        )
                        onSuccess()
                    },
                    onError = { message -> onError(message) }
                )
            }
        }
    }
}