package com.android.fitstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fitstore.checkout.PaymentLauncher

class MainActivity : ComponentActivity() {
    private lateinit var paymentLauncher: PaymentLauncher
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )
        super.onCreate(savedInstanceState)
        paymentLauncher = PaymentLauncher(this)
        paymentLauncher.initialize()
        setContent {
            App(androidLauncher = paymentLauncher)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}