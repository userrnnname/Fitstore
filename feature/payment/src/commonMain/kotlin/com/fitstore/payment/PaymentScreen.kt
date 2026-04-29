package com.fitstore.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitstore.shared.component.PrimaryButton
import com.fitstore.shared.util.formatPrice

@Composable
fun PaymentScreen(
    totalAmount: Double,
    orderId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    viewModel: PaymentViewModel
) {
    val paymentState by viewModel.paymentState.collectAsState()

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.Success -> onSuccess()
            is PaymentState.Error -> onError((paymentState as PaymentState.Error).message)
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Оплата заказа",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Сумма к оплате: ${totalAmount.formatPrice()} руб.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (paymentState) {
            is PaymentState.Loading -> {
                CircularProgressIndicator()
                Text("Подготовка платежа...")
            }

            is PaymentState.Ready -> {
                PrimaryButton(
                    text = "Оплатить через СБП",
                    onClick = {
                        viewModel.startPayment(totalAmount, orderId)
                    }
                )
            }

            is PaymentState.Error -> {
                Text(
                    text = (paymentState as PaymentState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.resetState() }) {
                    Text("Попробовать снова")
                }
            }

            else -> Unit
        }
    }
}