package com.fitstore.shared.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

class AndroidNotification(
    private val context: Context
) : PlatformNotification {

    override suspend fun requestPermission() {
        val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        Log.d("Notification", "Has permission: $isGranted")
    }

    override suspend fun getToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result
            continuation.resume(if (task.isSuccessful) task.result else null)
        }
    }

    override fun scheduleReminder(hours: Int) {
        val workRequest = OneTimeWorkRequestBuilder<ProteinReminderWorker>()
            .setInitialDelay(hours.toLong(), TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "protein_reminder",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}

actual fun createPlatformNotification(): PlatformNotification {
    val context: Context = GlobalContext.get().get()
    return AndroidNotification(context)
}