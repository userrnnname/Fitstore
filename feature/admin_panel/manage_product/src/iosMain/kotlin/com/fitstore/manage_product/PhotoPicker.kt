package com.fitstore.manage_product

import androidx.compose.runtime.Composable
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

actual class PhotoPicker {
    private var onSelect: ((ByteArray?) -> Unit)? = null

    actual fun open() {
        val config = PHPickerConfiguration()
        config.filter = PHPickerFilter.imagesFilter
        config.selectionLimit = 1

        val picker = PHPickerViewController(config)
        picker.delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult

                result?.itemProvider?.loadFileRepresentationForTypeIdentifier("public.image") { url, error ->
                    if (url != null) {
                        val data = NSData.dataWithContentsOfURL(url)
                        onSelect?.invoke(data?.toByteArray())
                    } else {
                        onSelect?.invoke(null)
                    }
                }
            }
        }

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, true, null)
    }

    @Composable
    actual fun InitializePhotoPicker(onImageSelect: (ByteArray?) -> Unit) {
        onSelect = onImageSelect
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
    return byteArray
}
