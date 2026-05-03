package com.fitstore.shared.notifications

class IosNotificationStub : PlatformNotification {
    override suspend fun requestPermission() {}
    override suspend fun getToken(): String? = null
    override fun scheduleReminder(hours: Int) {}
}

actual fun createPlatformNotification(): PlatformNotification = IosNotificationStub()