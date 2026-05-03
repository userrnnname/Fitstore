package com.fitstore.admin_panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.ButtonPrimary
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.IconSecondary
import com.fitstore.shared.K2DFont
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
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navigateBack: () -> Unit,
    navigateToManageProduct: (String?) -> Unit,
) {
    val viewModel = koinViewModel<AdminPanelViewModel>()
    val products = viewModel.filteredProducts.collectAsState()
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
                                text = "АДМИН-ПАНЕЛЬ",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToManageProduct(null) },
                containerColor = ButtonPrimary,
                contentColor = IconPrimary,
                content = {
                    Icon(
                        painter = painterResource(Resources.Icon.Plus),
                        contentDescription = "Добавить"
                    )
                }
            )
        }
    ) { padding ->
        products.value.DisplayResult(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { lastProducts ->
                AnimatedContent(
                    targetState = lastProducts
                ) { products ->
                    if (products.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(all = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = lastProducts,
                                key = { it.id!! }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { navigateToManageProduct(product.id) }
                                )
                            }
                        }
                    } else {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = "Упс!",
                            subtitle = "Товары не найдены."
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
            }
        )
    }
}