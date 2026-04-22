package com.android.fitstore

import androidx.compose.ui.window.ComposeUIViewController
import com.fitstore.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }