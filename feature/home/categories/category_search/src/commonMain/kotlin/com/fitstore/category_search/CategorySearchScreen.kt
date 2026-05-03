package com.fitstore.category_search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.fitstore.shared.domain.ProductCategory
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitstore.shared.K2DFont
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.IconSecondary
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.SurfaceSecondary
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.component.InfoCard
import com.fitstore.shared.component.LoadingCard
import com.fitstore.shared.component.ProductCard
import com.fitstore.shared.util.DisplayResult
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchScreen(
    category: ProductCategory,
    navigateToDetails: (String) -> Unit,
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<CategorySearchViewModel>()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var searchBarVisible by mutableStateOf(false)

    Scaffold(
        containerColor = Surface,
        topBar = {
            AnimatedContent(
                targetState = searchBarVisible
            ) { visible ->
                if (visible) {
                    SearchBar(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                        inputField = {
                            SearchBarDefaults.InputField(
                                modifier = Modifier.fillMaxWidth(),
                                query = searchQuery,
                                onQueryChange = viewModel::updateSearchQuery,
                                expanded = false,
                                onExpandedChange = {},
                                onSearch = {},
                                placeholder = {
                                    Text(
                                        text = "Поиск",
                                        fontSize = FontSize.REGULAR,
                                        color = TextPrimary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        modifier = Modifier.size(14.dp),
                                        onClick = {
                                            if (searchQuery.isNotEmpty()) viewModel.updateSearchQuery("")
                                            else searchBarVisible = false
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Close),
                                            contentDescription = "Закрыть"
                                        )
                                    }
                                }
                            )
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = SurfaceLighter,
                            dividerColor = BorderIdle,
                            inputFieldColors = SearchBarDefaults.inputFieldColors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedPlaceholderColor = TextPrimary.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = TextPrimary.copy(alpha = 0.5f),
                                focusedTrailingIconColor = TextPrimary,
                                unfocusedTrailingIconColor = TextPrimary,
                                focusedLeadingIconColor = TextPrimary,
                                unfocusedLeadingIconColor = TextPrimary,
                                cursorColor = TextPrimary,
                                selectionColors = TextSelectionColors(
                                    handleColor = IconSecondary,
                                    backgroundColor = SurfaceSecondary.copy(alpha = 0.4f)
                                )
                            )
                        ),
                        expanded = false,
                        onExpandedChange = {},
                        content = {}
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = category.title,
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
                            IconButton(onClick = { searchBarVisible = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Search),
                                    contentDescription = "Поиск",
                                    tint = IconPrimary
                                )
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
            }
        }
    ) { padding ->
        filteredProducts.DisplayResult(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { categoryProducts ->
                AnimatedContent(
                    targetState = categoryProducts
                ) { products ->
                    if (products.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(all = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id!! }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = navigateToDetails
                                )
                            }
                        }
                    } else {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = "Здесь ничего нет",
                            subtitle = "Мы не смогли найти ни одного товара."
                        )
                    }
                }
            },
            onError = { message ->
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Упс!",
                    subtitle = message
                )
            },
            transitionSpec = fadeIn() togetherWith fadeOut()
        )
    }
}