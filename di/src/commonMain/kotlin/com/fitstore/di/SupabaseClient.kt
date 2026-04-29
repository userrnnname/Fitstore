package com.fitstore.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.dsl.module

expect fun createSupabaseClient(): SupabaseClient
expect object SupabaseConfig {
    val url: String
    val anonKey: String
}

internal fun configureSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = SupabaseConfig.url,
        supabaseKey = SupabaseConfig.anonKey
    ) {
        install(Postgrest)
        install(Storage)
        install(Auth)
        install(Realtime)
    }
}

val supabaseModule = module {
    single { configureSupabaseClient() }
}