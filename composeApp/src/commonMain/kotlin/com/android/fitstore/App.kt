package com.android.fitstore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger.Companion.log
import com.fitstore.checkout.PaymentLauncher
import com.fitstore.data.domain.CustomerRepository
import com.fitstore.shared.navigation.Screen
import com.fitstore.navigation.SetupNavGraph
import com.fitstore.shared.notifications.PlatformNotification
import com.fitstore.shared.notifications.sendTokenToSupabase
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject


@Composable
@Preview
fun App(androidLauncher: PaymentLauncher? = null) {
    MaterialTheme {
        val customerRepository = koinInject<CustomerRepository>()
        var appReady by remember { mutableStateOf(false) }
        val userId by customerRepository.observeUserId().collectAsState(initial = null)
        val startDestination = remember(userId) {
            if (userId != null) Screen.HomeGraph else Screen.Auth
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
        val notifications = koinInject<PlatformNotification>()
        val supabaseClient = koinInject<SupabaseClient>()

        LaunchedEffect(userId) {
            if (userId !=null ) {
                notifications.requestPermission()
                val token = notifications.getToken()
                val userId = customerRepository.getCurrentUserId()
                if (token != null && userId != null) {
                    sendTokenToSupabase(token, supabaseClient, userId)
                }
            }
        }
    }
}