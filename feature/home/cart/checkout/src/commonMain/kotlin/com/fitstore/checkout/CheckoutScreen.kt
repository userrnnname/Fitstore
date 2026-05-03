package com.fitstore.checkout

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextWhite
import com.fitstore.shared.component.PrimaryButton
import com.fitstore.shared.component.ProfileForm
import com.fitstore.shared.util.formatPrice
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navigateBack: () -> Unit,
    navigateToPaymentCompleted: (Boolean?, String?, Double?) -> Unit,
    paymentLauncher: PaymentLauncher?
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CheckoutViewModel>{ parametersOf(paymentLauncher) }
    val screenState = viewModel.screenState
    val totalAmount by viewModel.totalAmount.collectAsState()
    val isFormValid = viewModel.isFormValid
    val isPaymentLoading = viewModel.isPaymentLoading
    LaunchedEffect(paymentLauncher) {
        paymentLauncher?.initialize()
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ПОКУПКА",
                        fontFamily = K2DFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                actions = {
                    AnimatedContent(targetState = totalAmount) { amount ->
                        Text(
                            text = if (amount > 0) "${amount.formatPrice()} руб." else "",
                            fontSize = FontSize.EXTRA_MEDIUM,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 12.dp,
                        bottom = 24.dp
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileForm(
                    modifier = Modifier.weight(1f),
                    lastName = screenState.lastName,
                    onLastNameChange = viewModel::updateLastName,
                    firstName = screenState.firstName,
                    onFirstNameChange = viewModel::updateFirstName,
                    email = screenState.email,
                    city = screenState.city,
                    onCityChange = viewModel::updateCity,
                    postalCode = screenState.postalCode,
                    onPostalCodeChange = viewModel::updatePostalCode,
                    address = screenState.address,
                    onAddressChange = viewModel::updateAddress,
                    phoneNumber = screenState.phoneNumber?.number,
                    onPhoneNumberChange = viewModel::updatePhoneNumber
                )
                Column {
                    PrimaryButton(
                        text = if (viewModel.isPaymentLoading) "Загрузка..." else "Оплатить онлайн",
                        icon = Resources.Icon.CreditCard,
                        enabled = isFormValid && !isPaymentLoading && totalAmount > 0,
                        onClick = {
                            viewModel.startOnlinePayment(
                                onSuccess = { amount ->
                                    navigateToPaymentCompleted(true, null, amount)
                                },
                                onError = { error ->
                                    navigateToPaymentCompleted(false, error, null)
                                }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Оплата при доставке",
                        icon = Resources.Icon.ShoppingCart,
                        secondary = true,
                        enabled = isFormValid && !isPaymentLoading && totalAmount > 0,
                        onClick = {
                            viewModel.payOnDelivery(
                                onSuccess = {
                                    navigateToPaymentCompleted(true, null, totalAmount)
                                },
                                onError = { error ->
                                    navigateToPaymentCompleted(null, error, null)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}