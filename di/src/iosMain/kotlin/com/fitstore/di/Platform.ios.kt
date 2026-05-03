package com.fitstore.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun platformHttpEngine(): HttpClientEngine {
    return Darwin.create {

    }
}