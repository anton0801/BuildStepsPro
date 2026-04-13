package com.buildSteps.BuildStepsPro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.apply
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.jvm.java
import kotlin.let
import kotlin.to


class BuildStepsProNotificationsService : FirebaseMessagingService() {

    companion object {
        private const val BATCH_MANAGER_CHANNEL_ID = "build_steps_pro_notifications_channel"
        private const val SLEEP_RELAX_CHANNEL_NAME = "Build Steps Pro Notifications"
        private const val SLEEP_RELAX_NOT_TAG = "Build Steps Pro"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                eggLabelShowNotification(it.title ?: SLEEP_RELAX_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                eggLabelShowNotification(it.title ?: SLEEP_RELAX_NOT_TAG, it.body ?: "", data = null)
            }
        }

        if (remoteMessage.data.isNotEmpty()) {
            eggLabelHandleDataPayload(remoteMessage.data)
        }
    }

    private fun eggLabelShowNotification(title: String, message: String, data: String?) {
        val eggLabelNotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BATCH_MANAGER_CHANNEL_ID,
                SLEEP_RELAX_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            eggLabelNotificationManager.createNotificationChannel(channel)
        }

        val eggLabelIntent = Intent(this, MainStartActivity::class.java).apply {
            putExtras(
                bundleOf(
                    "url" to data
                )
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val eggLabelPendingIntent = PendingIntent.getActivity(
            this,
            0,
            eggLabelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val eggLabelNotification = NotificationCompat.Builder(this, BATCH_MANAGER_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications_icon)
            .setAutoCancel(true)
            .setContentIntent(eggLabelPendingIntent)
            .build()

        eggLabelNotificationManager.notify(System.currentTimeMillis().toInt(), eggLabelNotification)
    }

    private fun eggLabelHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
        }
    }

}