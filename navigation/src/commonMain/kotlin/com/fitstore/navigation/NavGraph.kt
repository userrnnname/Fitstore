package com.fitstore.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fitstore.admin_panel.AdminPanelScreen
import com.fitstore.auth.AuthScreen
import com.fitstore.category_search.CategorySearchScreen
import com.fitstore.checkout.CheckoutScreen
import com.fitstore.details.DetailsScreen
import com.fitstore.edit_profile.EditProfileScreen
import com.fitstore.home.HomeGraphScreen
import com.fitstore.login.LoginScreen
import com.fitstore.manage_product.ManageProductScreen
import com.fitstore.payment_completed.PaymentCompletedScreen
import com.fitstore.profile.ProfileScreen
import com.fitstore.register.RegisterScreen
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.navigation.Screen

@Composable
fun SetupNavGraph(startDestination: Screen = Screen.Auth) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(Screen.Login)
                }
            )
        }
        composable<Screen.Login> {
            LoginScreen(
                navigateBack = { navController.navigateUp() },
                navigateToRegister = { navController.navigate(Screen.Register) },
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Register> {
            RegisterScreen(
                navigateBack = { navController.navigateUp() },
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.HomeGraph> {
            HomeGraphScreen(
                navigateToAuth = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                navigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel)
                },
                navigateToDetails = { productId ->
                    navController.navigate(Screen.Details(id = productId))
                },
                navigateToCategorySearch = { categoryName ->
                    navController.navigate(Screen.CategorySearch(categoryName))
                },
                navigateToCheckout =  { totalAmount ->
                    navController.navigate(Screen.Checkout)
                }
            )
        }
        composable<Screen.Profile> {
            ProfileScreen(
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToEditProfile = {
                    navController.navigate(Screen.EditProfile)
                }
            )
        }
        composable<Screen.EditProfile> {
            EditProfileScreen(
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id = id))
                }
            )
        }
        composable<Screen.ManageProduct> {
            val id = it.toRoute<Screen.ManageProduct>().id
            ManageProductScreen(
                id = id,
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.Details> {
            DetailsScreen(
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.CategorySearch> {
            val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
            CategorySearchScreen(
                category = category,
                navigateToDetails = { id ->
                    navController.navigate(Screen.Details(id))
                },
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.Checkout> {
            CheckoutScreen(
                navigateBack = { navController.navigateUp() },
                navigateToPaymentCompleted = { isSuccess, error ->
                    navController.navigate(Screen.PaymentCompleted(isSuccess, error))
                }
            )
        }
        composable<Screen.PaymentCompleted> {
            PaymentCompletedScreen(
                navigateBack = {
                    navController.navigate(Screen.HomeGraph) {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}