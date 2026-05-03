package com.fitstore.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@OptIn(SupabaseInternal::class)
fun createAndConfigureSupabaseClient(
    supabaseUrl: String,
    supabaseKey: String,
    httpEngine: HttpClientEngine
): SupabaseClient {
    return createSupabaseClient(supabaseUrl, supabaseKey) {
        this.httpEngine = httpEngine
        httpConfig {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) { level = LogLevel.ALL }
            install(WebSockets)
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
        }
        install(Postgrest)
        install(Storage)
        install(Auth)
        install(Realtime)
        install(Functions)
    }
}
