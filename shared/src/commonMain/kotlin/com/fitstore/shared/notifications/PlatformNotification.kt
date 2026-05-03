package com.fitstore.shared.notifications

interface PlatformNotification {
    suspend fun requestPermission()
    suspend fun getToken(): String?
    fun scheduleReminder(hours: Int)
}

expect fun createPlatformNotification(): PlatformNotification