package com.fitstore.home

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitstore.cart.CartScreen
import com.fitstore.cart.CartViewModel
import com.fitstore.categories.CategoriesScreen
import com.fitstore.home.component.BottomBar
import com.fitstore.home.component.CustomDrawer
import com.fitstore.home.domain.BottomBarDestination
import com.fitstore.home.domain.CustomDrawerState
import com.fitstore.home.domain.isOpened
import com.fitstore.home.domain.opposite
import com.fitstore.products_overview.ProductsOverviewScreen
import com.fitstore.shared.Alpha
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextWhite
import com.fitstore.shared.navigation.Screen
import com.fitstore.shared.util.RequestState
import com.fitstore.shared.util.getScreenWidth
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGraphScreen(
    navigateToAuth: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAdminPanel: () -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (String) -> Unit,
    navigateToCheckout: (String) -> Unit
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()
    val selectedDestination by remember {
        derivedStateOf {
            val route = currentRoute.value?.destination?.route.toString()
            when {
                route.contains(BottomBarDestination.ProductsOverview.screen.toString()) -> BottomBarDestination.ProductsOverview
                route.contains(BottomBarDestination.Cart.screen.toString()) -> BottomBarDestination.Cart
                route.contains(BottomBarDestination.Categories.screen.toString()) -> BottomBarDestination.Categories
                else -> BottomBarDestination.ProductsOverview
            }
        }
    }

    val screenWidth = remember { getScreenWidth() }
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }

    val offsetValue by remember { derivedStateOf { (screenWidth / 1.5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp
    )

    val animatedBackground by animateColorAsState(
        targetValue = if (drawerState.isOpened()) SurfaceLighter else Surface
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f
    )

    val animatedRadius by animateDpAsState(
        targetValue = if (drawerState.isOpened()) 20.dp else 0.dp
    )

    val viewModel = koinViewModel<HomeGraphViewModel>()
    val customer by viewModel.customer.collectAsState()
    val cartViewModel = koinViewModel<CartViewModel>()
    val totalAmount by cartViewModel.totalAmountFlow.collectAsState(RequestState.Loading)
    val messageBarState = rememberMessageBarState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackground)
            .systemBarsPadding()
    ) {
        CustomDrawer(
            customer = customer,
            onProfileClick = navigateToProfile,
            onContactUsClick = {},
            onSignOutClick = {
                viewModel.signOut(
                    onSuccess = navigateToAuth,
                    onError = { message -> messageBarState.addError(message) }
                )
                             },
            onAdminPanelClick = navigateToAdminPanel
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(size = animatedRadius))
                .offset(x = animatedOffset)
                .scale(scale = animatedScale)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(size = animatedRadius),
                    ambientColor = Color.Black.copy(alpha = Alpha.DISABLED),
                    spotColor = Color.Black.copy(alpha = Alpha.DISABLED)
                )
        ) {
            Scaffold(
                containerColor = Surface,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            AnimatedContent(
                                targetState = selectedDestination
                            ) { destination ->
                                Text(
                                    text = destination.title,
                                    fontFamily = K2DFont(),
                                    fontSize = FontSize.LARGE,
                                    color = TextPrimary
                                )
                            }
                        },
                        actions = {
                            AnimatedVisibility(
                                visible = selectedDestination == BottomBarDestination.Cart
                            ) {
                                if (customer.isSuccess() && customer.getSuccessData().cart.isNotEmpty()) {
                                    IconButton(onClick = {
                                        if (totalAmount.isSuccess()) {
                                            navigateToCheckout(
                                                totalAmount.getSuccessData().toString()
                                            )
                                        } else if (totalAmount.isError()) {
                                            messageBarState.addError("Ошибка при расчете общей суммы: ${totalAmount.getErrorMessage()}")
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.RightArrow),
                                            contentDescription = "Вправо",
                                            tint = IconPrimary
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            AnimatedContent(
                                targetState = drawerState
                            ) { drawer ->
                                if (drawer.isOpened()) {
                                    IconButton(onClick = { drawerState = drawerState.opposite() }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Close),
                                            contentDescription = "Закрыть",
                                            tint = IconPrimary
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { drawerState = drawerState.opposite() }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Menu),
                                            contentDescription = "Меню",
                                            tint = IconPrimary
                                        )
                                    }
                                }
                            }
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
                ContentWithMessageBar(
                    contentBackgroundColor = Surface,
                    modifier = Modifier
                        .fillMaxSize()
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            modifier = Modifier.weight(1f),
                            navController = navController,
                            startDestination = Screen.ProductsOverview
                        ) {
                            composable<Screen.ProductsOverview> {
                                ProductsOverviewScreen(
                                    navigateToDetails = navigateToDetails
                                )
                            }
                            composable<Screen.Cart> {
                                CartScreen()
                            }
                            composable<Screen.Categories> {
                                CategoriesScreen(
                                    navigateToCategorySearch =  navigateToCategorySearch
                                )

                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .padding(all = 12.dp)
                        ) {
                            BottomBar(
                                customer = customer,
                                selected = selectedDestination,
                                onSelect = { destination ->
                                    navController.navigate(destination.screen) {
                                        launchSingleTop = true
                                        popUpTo<Screen.ProductsOverview> {
                                            saveState = true
                                            inclusive = false
                                        }
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}