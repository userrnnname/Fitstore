package com.fitstore.shared.notifications

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

suspend fun sendTokenToSupabase(
    token: String,
    supabase: SupabaseClient,
    userId: String
) {
    supabase.postgrest["fcm_tokens"].upsert(
        mapOf("user_id" to userId, "token" to token)
    )
}