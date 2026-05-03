package com.fitstore.manage_product

import ContentWithMessageBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.ButtonPrimary
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceDarker
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.SurfaceSecondary
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.TextWhite
import com.fitstore.shared.component.AlertTextField
import com.fitstore.shared.component.CustomTextField
import com.fitstore.shared.component.ErrorCard
import com.fitstore.shared.component.LoadingCard
import com.fitstore.shared.component.PrimaryButton
import com.fitstore.shared.component.dialog.CategoriesDialog
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.util.DisplayResult
import com.fitstore.shared.util.RequestState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(
    id: String?,
    navigateBack: () -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<ManageProductViewModel>()
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid
    val thumbnailUploaderState = viewModel.thumbnailUploaderState
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var dropdownMenuOpened by remember { mutableStateOf(false) }

    val photoPicker = koinInject<PhotoPicker>()

    photoPicker.InitializePhotoPicker(
        onImageSelect = { bytes ->
            if (bytes != null) {
                viewModel.uploadThumbnailToStorage(
                    byteArray = bytes,
                    onSuccess = {
                        messageBarState.addSuccess("Миниатюра успешно загружена!")
                    }
                )
            }
        }
    )

    AnimatedVisibility(
        visible = showCategoriesDialog
    ) {
        CategoriesDialog(
            category = screenState.category,
            onDismiss = { showCategoriesDialog = false },
            onConfirmClick = { selectedCategory ->
                viewModel.updateCategory(selectedCategory)
                showCategoriesDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (id == null) "НОВЫЙ ТОВАР"
                        else "ИЗМЕНИТЬ ТОВАР",
                        fontFamily = K2DFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Назад",
                            tint = IconPrimary
                        )
                    }
                },
                actions = {
                    id.takeIf { it != null }?.let {
                        Box {
                            IconButton(onClick = { dropdownMenuOpened = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.VerticalMenu),
                                    contentDescription = "Вертикальное меню",
                                    tint = IconPrimary
                                )
                            }
                            DropdownMenu(
                                containerColor = Surface,
                                expanded = dropdownMenuOpened,
                                onDismissRequest = { dropdownMenuOpened = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Delete),
                                            contentDescription = "Удалить",
                                            tint = IconPrimary
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Удалить",
                                            color = TextPrimary,
                                            fontSize = FontSize.REGULAR
                                        )
                                    },
                                    onClick = {
                                        dropdownMenuOpened = false
                                        viewModel.deleteProduct(
                                            onSuccess = navigateBack,
                                            onError = { message -> messageBarState.addError(message) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            contentBackgroundColor = Surface,
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = SurfaceError,
            errorContentColor = TextWhite,
            successContainerColor = SurfaceBrand,
            successContentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(
                        bottom = 24.dp,
                        top = 12.dp
                    )
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(size = 12.dp))
                            .border(
                                width = 1.dp,
                                color = BorderIdle,
                                shape = RoundedCornerShape(size = 12.dp)
                            )
                            .background(SurfaceLighter)
                            .clickable(
                                enabled = thumbnailUploaderState.isIdle()
                            ) {
                                println("Включено!")
                                photoPicker.open()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        thumbnailUploaderState.DisplayResult(
                            onIdle = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(Resources.Icon.Plus),
                                    contentDescription = "Плюс",
                                    tint = IconPrimary
                                )
                            },
                            onLoading = {
                                LoadingCard(modifier = Modifier.fillMaxSize())
                            },
                            onSuccess = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    AsyncImage(
                                        modifier = Modifier.fillMaxSize(),
                                        model = ImageRequest.Builder(
                                            LocalPlatformContext.current
                                        ).data(screenState.thumbnail)
                                            .crossfade(enable = true)
                                            .build(),
                                        contentDescription = "Миниатюра",
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(size = 6.dp))
                                            .padding(
                                                top = 12.dp,
                                                end = 12.dp
                                            )
                                            .background(ButtonPrimary)
                                            .clickable {
                                                viewModel.deleteThumbnailFromStorage(
                                                    onSuccess = { messageBarState.addSuccess("Миниатюра успешно удалена.") },
                                                    onError = { message ->
                                                        messageBarState.addError(
                                                            message
                                                        )
                                                    }
                                                )
                                            }
                                            .padding(all = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Delete),
                                            contentDescription = "Удалить"
                                        )
                                    }
                                }
                            },
                            onError = { message ->
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ErrorCard(message = message)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(
                                        onClick = {
                                            viewModel.updateThumbnailUploaderState(RequestState.Idle)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Text(
                                            text = "Попробовать снова",
                                            fontSize = FontSize.SMALL,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        )
                    }
                    CustomTextField(
                        value = screenState.title,
                        onValueChange = viewModel::updateTitle,
                        placeholder = "Название"
                    )
                    CustomTextField(
                        modifier = Modifier.height(168.dp),
                        value = screenState.description,
                        onValueChange = viewModel::updateDescription,
                        placeholder = "Описание",
                        expanded = true
                    )
                    AlertTextField(
                        modifier = Modifier.fillMaxWidth(),
                        text = screenState.category.title,
                        onClick = { showCategoriesDialog = true }
                    )
                    AnimatedVisibility(
                        visible = screenState.category != ProductCategory.Accessories
                    ) {
                        Column {
                            CustomTextField(
                                value = screenState.weightString,
                                onValueChange = viewModel::updateWeightString,
                                placeholder = "Вес",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CustomTextField(
                                value = screenState.flavors,
                                onValueChange = viewModel::updateFlavors,
                                placeholder = "Вкусы"
                            )
                        }
                    }
                    CustomTextField(
                        value = screenState.priceString,
                        onValueChange = viewModel::updatePriceString,
                        placeholder = "Цена",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    CustomTextField(
                        value = screenState.servingsString,
                        onValueChange = viewModel::updateServingsString,
                        placeholder = "Количество порций",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = "Новое",
                                fontSize = FontSize.REGULAR,
                                color = TextPrimary
                            )
                            Switch(
                                checked = screenState.isNew,
                                onCheckedChange = viewModel::updateNew,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SurfaceSecondary,
                                    uncheckedTrackColor = SurfaceDarker,
                                    checkedThumbColor = Surface,
                                    uncheckedThumbColor = Surface,
                                    checkedBorderColor = SurfaceSecondary,
                                    uncheckedBorderColor = SurfaceDarker
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = "Популярное",
                                fontSize = FontSize.REGULAR,
                                color = TextPrimary
                            )
                            Switch(
                                checked = screenState.isPopular,
                                onCheckedChange = viewModel::updatePopular,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SurfaceSecondary,
                                    uncheckedTrackColor = SurfaceDarker,
                                    checkedThumbColor = Surface,
                                    uncheckedThumbColor = Surface,
                                    checkedBorderColor = SurfaceSecondary,
                                    uncheckedBorderColor = SurfaceDarker
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = "Со скидкой",
                                fontSize = FontSize.REGULAR,
                                color = TextPrimary
                            )
                            Switch(
                                checked = screenState.isDiscounted,
                                onCheckedChange = viewModel::updateDiscounted,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SurfaceSecondary,
                                    uncheckedTrackColor = SurfaceDarker,
                                    checkedThumbColor = Surface,
                                    uncheckedThumbColor = Surface,
                                    checkedBorderColor = SurfaceSecondary,
                                    uncheckedBorderColor = SurfaceDarker
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                PrimaryButton(
                    text = if (id == null) "Добавить новый товар"
                    else "Обновить",
                    icon = if (id == null) Resources.Icon.Plus
                    else Resources.Icon.Checkmark,
                    enabled = isFormValid,
                    onClick = {
                        if (id != null) {
                            viewModel.updateProduct(
                                onSuccess = { messageBarState.addSuccess("Товар успешно обновлён!") },
                                onError = { message -> messageBarState.addError(message) }
                            )
                        } else {
                            viewModel.createNewProduct(
                                onSuccess = { messageBarState.addSuccess("Товар успешно добавлен!") },
                                onError = { message -> messageBarState.addError(message) }
                            )
                        }
                    }
                )
            }
        }
    }
}