package com.fitstore.di

import io.github.jan.supabase.SupabaseClient

actual object SupabaseConfig {
    actual val url: String = "https://bvmamyusputqijqsokdt.supabase.co"
    actual val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2bWFteXVzcHV0cWlqcXNva2R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3NzM3MjEsImV4cCI6MjA5MjM0OTcyMX0.t65CvtX7sNyA_UI5Yo2ztmcMOLPCNhCgbLxc8wmtmaE"
}

actual fun createSupabaseClient(): SupabaseClient {
    return configureSupabaseClient()
}
