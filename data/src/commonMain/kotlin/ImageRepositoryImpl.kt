package com.fitstore.data

import com.fitstore.data.domain.ImageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

class ImageRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ImageRepository {

    override suspend fun uploadImage(
        fileName: String,
        byteArray: ByteArray,
        bucketName: String
    ): String {
        val bucket = supabaseClient.storage.from(bucketName)
        val fullPath = "$fileName.jpg"

        bucket.upload(path = fullPath, data = byteArray) {
            upsert = true
        }

        return bucket.publicUrl(fullPath)
    }

    override suspend fun deleteImage(
        fileName: String,
        bucketName: String
    ) {
        val bucket = supabaseClient.storage.from(bucketName)
        bucket.delete(listOf("$fileName.jpg"))
    }
}
