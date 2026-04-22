package com.fitstore.manage_product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class PhotoPicker {
    private var launcher: (() -> Unit)? = null

    actual fun open() {
        launcher?.invoke()
    }

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray?) -> Unit) {
        val context = LocalContext.current
        val pickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                onImageSelect(bytes)
            } else {
                onImageSelect(null)
            }
        }

        launcher = {
            pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
