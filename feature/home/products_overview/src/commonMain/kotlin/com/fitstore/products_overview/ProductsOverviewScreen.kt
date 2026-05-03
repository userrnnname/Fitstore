package com.fitstore.products_overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitstore.products_overview.component.MainProductCard
import com.fitstore.shared.Alpha
import com.fitstore.shared.FontSize
import com.fitstore.shared.Resources
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.component.InfoCard
import com.fitstore.shared.component.LoadingCard
import com.fitstore.shared.component.ProductCard
import com.fitstore.shared.util.DisplayResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductsOverviewScreen(
    navigateToDetails: (String) -> Unit
) {
    val viewModel = koinViewModel<ProductsOverviewViewModel>()
    val products by viewModel.products.collectAsState()
    val listState = rememberLazyListState()

    val centeredIndex: Int? by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }?.index
        }
    }
    products.DisplayResult(
        onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
        onSuccess = { productList ->
            val products = productList.distinctBy { it.id }

            if (products.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(
                                    items = products.filter { it.isNew }.sortedBy { it.createdAt }.take(6),
                                    key = { _, item -> item.id!! }
                                ) { index, product ->
                                    val isLarge = index == centeredIndex
                                    val animatedScale by animateFloatAsState(
                                        targetValue = if (isLarge) 1f else 0.8f,
                                        animationSpec = tween(300)
                                    )
                                    MainProductCard(
                                        modifier = Modifier
                                            .scale(animatedScale)
                                            .height(300.dp)
                                            .fillParentMaxWidth(0.6f),
                                        product = product,
                                        isLarge = isLarge,
                                        onClick = { navigateToDetails(product.id!!) }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth().alpha(Alpha.HALF),
                            text = "Со скидкой",
                            fontSize = FontSize.EXTRA_REGULAR,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(
                        items = products.filter { it.isDiscounted }.sortedBy { it.createdAt }.take(3),
                        key = { it.id!! }
                    ) { product ->
                        ProductCard(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            product = product,
                            onClick = { navigateToDetails(product.id!!) }
                            )
                        }
                    }
                } else {
                    InfoCard(
                        image = Resources.Image.Cat,
                        title = "Здесь ничего нет",
                        subtitle = "Пустой список товаров."
                    )
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