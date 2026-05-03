package com.fitstore.data.domain

interface ImageRepository {

    suspend fun uploadImage(
        fileName: String,
        byteArray: ByteArray,
        bucketName: String = "product-images"
    ): String

    suspend fun deleteImage(
        fileName: String,
        bucketName: String = "product-images"
    )

    suspend fun updateImage(
        fileName: String,
        byteArray: ByteArray,
        bucketName: String = "product-images"
    ): String {

        return uploadImage(fileName, byteArray, bucketName)
    }
}