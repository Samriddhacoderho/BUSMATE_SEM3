package com.example.busmate.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.busmate.R
import com.example.busmate.view.dashboard.ParentDashboardActivity

object NotificationHelper {
    private const val CHANNEL_ID = "bus_alerts_channel"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create the Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "bus_alerts_channel", // Must match the ID used in the Builder
                "Bus Alerts",
                NotificationManager.IMPORTANCE_HIGH // REQUIRED for the pop-up banner
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 2. Setup what happens when they tap the notification
        val intent = Intent(context, ParentDashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.schoolbus) // Make sure this icon exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For older Android versions
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Sound and Vibrate
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // 4. Fire the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}