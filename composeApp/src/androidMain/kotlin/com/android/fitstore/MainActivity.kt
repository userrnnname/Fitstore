package com.android.fitstore

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fitstore.shared.payment.PaymentLauncher

class MainActivity : ComponentActivity() {
    private lateinit var paymentLauncher: PaymentLauncher
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb()
            )
        )
        super.onCreate(savedInstanceState)
        paymentLauncher = PaymentLauncher(
            clientApplicationKey = "live_ВАШ_КЛЮЧ_ДЛЯ_SDK",
            shopId = "ВАШ_SHOP_ID"
        )
        paymentLauncher.attachActivity(this)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Логика обработки разрешения
        }

        setContent {
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            App(paymentLauncher = paymentLauncher)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (::paymentLauncher.isInitialized) {
            paymentLauncher.processActivityResult(requestCode, resultCode, data)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {

}