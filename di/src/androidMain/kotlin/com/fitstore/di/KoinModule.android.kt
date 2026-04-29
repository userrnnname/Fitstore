package com.fitstore.di

import androidx.activity.ComponentActivity
import com.fitstore.checkout.PaymentLauncher
import com.fitstore.manage_product.PhotoPicker
import org.koin.dsl.module

actual val targetModule = module {
    single<PhotoPicker> { PhotoPicker() }
    single { (activity: ComponentActivity) -> PaymentLauncher(activity) }
}