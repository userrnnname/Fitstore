package com.android.fitstore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fitstore.checkout.PaymentLauncher
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.navigation.Screen
import com.fitstore.navigation.SetupNavGraph
import org.koin.compose.koinInject

@Composable
@Preview
fun App(androidLauncher: PaymentLauncher? = null) {
    MaterialTheme {
        val customerRepository = koinInject<CustomerRepository>()
        var appReady by remember { mutableStateOf(false) }
        val isUserAuthenticated = remember { customerRepository.getCurrentUserId() != null }
        val startDestination = remember {
            if (isUserAuthenticated) Screen.HomeGraph
            else Screen.Auth
        }

        LaunchedEffect(Unit) {
            appReady = true
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = appReady
        ) {
            SetupNavGraph(
                startDestination = startDestination,
                paymentLauncher = androidLauncher
            )
        }
    }
}