package com.fitstore.manage_product

import androidx.compose.runtime.Composable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhotoPicker {
    fun open()

    @Composable
    fun InitializePhotoPicker(onImageSelect: (ByteArray?) -> Unit)
}