package com.fitstore.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        }
    }
}