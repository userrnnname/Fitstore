package com.fitstore.details

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.domain.ProductCategory
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import com.fitstore.details.component.FlavorChip
import com.fitstore.shared.K2DFont
import com.fitstore.shared.RobotoCondensedFont
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.TextWhite
import com.fitstore.shared.component.InfoCard
import com.fitstore.shared.component.LoadingCard
import com.fitstore.shared.component.PrimaryButton
import com.fitstore.shared.component.QuantityCounter
import com.fitstore.shared.domain.QuantityCounterSize
import com.fitstore.shared.util.DisplayResult
import com.fitstore.shared.util.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navigateBack: () -> Unit) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<DetailsViewModel>()
    val product by viewModel.product.collectAsState()
    val quantity = viewModel.quantity
    val selectedFlavor = viewModel.selectedFlavor

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ДЕТАЛИ",
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
                    QuantityCounter(
                        size = QuantityCounterSize.Large,
                        value = quantity,
                        onMinusClick = viewModel::updateQuantity,
                        onPlusClick = viewModel::updateQuantity
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        }
    ) { padding ->
        product.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { selectedProduct ->
                ContentWithMessageBar(
                    contentBackgroundColor = Surface,
                    modifier = Modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        ),
                    messageBarState = messageBarState,
                    errorMaxLines = 2,
                    errorContainerColor = SurfaceError,
                    errorContentColor = TextWhite,
                    successContainerColor = SurfaceBrand,
                    successContentColor = TextPrimary
                ) {
                    Column {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(top = 12.dp)
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(size = 12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = BorderIdle,
                                        shape = RoundedCornerShape(size = 12.dp)
                                    ),
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(selectedProduct.thumbnail)
                                    .crossfade(enable = true)
                                    .build(),
                                contentDescription = "Миниатюра",
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AnimatedContent(
                                    targetState = selectedProduct.category
                                ) { category ->
                                    if (ProductCategory.fromString(category) == ProductCategory.Accessories) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(14.dp),
                                                painter = painterResource(Resources.Icon.Weight),
                                                contentDescription = "Вес"
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${selectedProduct.weight} гр.",
                                                fontSize = FontSize.REGULAR,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${selectedProduct.price.formatPrice()} руб.",
                                    fontSize = FontSize.MEDIUM,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = selectedProduct.title,
                                fontSize = FontSize.EXTRA_MEDIUM,
                                fontWeight = FontWeight.Medium,
                                fontFamily = RobotoCondensedFont(),
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = selectedProduct.description,
                                fontSize = FontSize.REGULAR,
                                lineHeight = FontSize.REGULAR * 1.3,
                                color = TextPrimary
                            )
                        }
                        val flavorsList = selectedProduct.flavors
                        val isFlavorSelectionRequired = !flavorsList.isNullOrEmpty() && flavorsList.any { it.isNotBlank() }
                        Column(
                            modifier = Modifier
                                .background(
                                    if (isFlavorSelectionRequired) SurfaceLighter
                                    else Surface
                                )
                                .padding(all = 24.dp)
                        ) {
                            if (isFlavorSelectionRequired) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    selectedProduct.flavors?.forEach { flavor ->
                                        if (flavor.isNotBlank()) {
                                            FlavorChip(
                                                flavor = flavor,
                                                isSelected = selectedFlavor == flavor,
                                                onClick = { viewModel.updateFlavor(flavor) }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            PrimaryButton(
                                icon = Resources.Icon.ShoppingCart,
                                text = "Добавить в корзину",
                                enabled = if (isFlavorSelectionRequired) selectedFlavor != null
                                else true,
                                onClick = {
                                    viewModel.addItemToCart(
                                        onSuccess = { messageBarState.addSuccess("Товар добавлен в корзину.") },
                                        onError = { message -> messageBarState.addError(message) }
                                    )
                                }
                            )
                        }
                    }
                }
            },
            onError = { message ->
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Упс!",
                    subtitle = message
                )
            }
        )
    }
}