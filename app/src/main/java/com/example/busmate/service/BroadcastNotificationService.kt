package com.example.busmate.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.busmate.R
import com.example.busmate.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Handles Admin broadcast notifications
 * Runs for Parent and Driver. Explicitly stops if user is Admin.
 */
class BroadcastNotificationService : Service() {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var globalBroadcastQuery: Query? = null
    private var globalBroadcastListener: ValueEventListener? = null

    private var currentUserId: String? = null
    private var currentUserType: String? = null

    companion object {
        private const val CHANNEL_ID = "broadcast_channel"
        private const val NOTIFICATION_ID = 202
        private const val TAG = "BroadcastService"
        private const val PREFS_NAME = "BroadcastNotifications"
        private const val KEY_LAST_PROCESSED_PREFIX = "last_processed_broadcast_"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "✅ BroadcastNotificationService CREATED")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground notification to keep service alive
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BusMate Status")
            .setContentText("Listening for school announcements...")
            .setSmallIcon(R.drawable.outline_directions_bus_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e(TAG, "❌ No authenticated user. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        currentUserId = uid
        Log.d(TAG, "📱 Service started for UID: $uid")

        removeOldListener()
        checkUserTypeAndSetupListener(uid)

        return START_STICKY
    }

    private fun checkUserTypeAndSetupListener(uid: String) {
        Log.d(TAG, "🔍 Fetching user type from Firebase...")

        // Fetch the user type from the 'users' node
        database.getReference("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e(TAG, "❌ User snapshot does not exist for UID: $uid")
                    stopSelf()
                    return@addOnSuccessListener
                }

                // Ensure field name matches your Firebase (typeofUser vs userType)
                val userType = snapshot.child("typeofUser").getValue(String::class.java) ?: ""
                currentUserType = userType

                Log.d(TAG, "👤 User Type Detected: '$userType'")

                // Case-insensitive check and explicit Admin exclusion
                when {
                    userType.equals("Admin", ignoreCase = true) -> {
                        Log.d(TAG, "🚫 User is Admin. Stopping Broadcast Service.")
                        stopSelf()
                    }
                    userType.equals("Parent", ignoreCase = true) -> {
                        Log.d(TAG, "📡 Setting up listener for Parent")
                        setupGlobalBroadcastListener(uid)
                    }
                    userType.equals("Driver", ignoreCase = true) -> {
                        Log.d(TAG, "📡 Setting up listener for Driver")
                        setupGlobalBroadcastListener(uid)
                    }
                    else -> {
                        Log.e(TAG, "❓ Unknown user type: '$userType'. Stopping service.")
                        stopSelf()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to fetch user type: ${exception.message}")
                exception.printStackTrace()
                stopSelf()
            }
    }

    private fun setupGlobalBroadcastListener(userId: String) {
        Log.d(TAG, "🎯 Setting up global broadcast listener for user: $userId")

        // Ensure this matches the path used in AdminActionsImpl
        val globalRef = database.getReference("notifications/global")

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Create user-specific key so each user tracks their own notifications
        val userSpecificKey = KEY_LAST_PROCESSED_PREFIX + userId

        // FIX: Use 0L instead of current time so user gets ALL existing broadcasts
        // Change to System.currentTimeMillis() if you want to ignore old notifications
        val lastProcessedTimestamp = prefs.getLong(userSpecificKey, 0L)

        Log.d(TAG, "📊 Last processed timestamp: $lastProcessedTimestamp")

        globalBroadcastListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "📬 Firebase data changed. Checking for broadcasts...")

                if (!snapshot.exists()) {
                    Log.d(TAG, "📭 No broadcasts exist in database")
                    return
                }

                var latestProcessedInThisBatch = lastProcessedTimestamp
                var newNotificationsCount = 0

                Log.d(TAG, "📦 Found ${snapshot.childrenCount} total broadcasts")

                for (broadcast in snapshot.children) {
                    val broadcastId = broadcast.key ?: "unknown"
                    val timestamp = broadcast.child("timestamp").getValue(Long::class.java) ?: 0L
                    val title = broadcast.child("title").getValue(String::class.java) ?: "Announcement"
                    val message = broadcast.child("message").getValue(String::class.java) ?: ""

                    Log.d(TAG, "🔍 Checking broadcast: ID=$broadcastId, timestamp=$timestamp, lastProcessed=$lastProcessedTimestamp")

                    // Only process messages newer than our saved timestamp
                    if (timestamp > lastProcessedTimestamp) {
                        Log.d(TAG, "🔔 NEW BROADCAST FOUND!")
                        Log.d(TAG, "   Title: $title")
                        Log.d(TAG, "   Message: $message")
                        Log.d(TAG, "   Timestamp: $timestamp")

                        newNotificationsCount++

                        // Show System Notification
                        try {
                            NotificationHelper.showNotification(applicationContext, title, message)
                            Log.d(TAG, "✅ Notification shown successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Failed to show notification: ${e.message}")
                            e.printStackTrace()
                        }

                        // Save to history node for the specific user
                        saveNotificationToHistory(userId, title, message)

                        if (timestamp > latestProcessedInThisBatch) {
                            latestProcessedInThisBatch = timestamp
                        }
                    } else {
                        Log.d(TAG, "⏭️ Skipping old broadcast: $title (timestamp: $timestamp)")
                    }
                }

                // Update Preference with user-specific key so we don't show these again
                if (latestProcessedInThisBatch > lastProcessedTimestamp) {
                    prefs.edit().putLong(userSpecificKey, latestProcessedInThisBatch).apply()
                    Log.d(TAG, "💾 Updated last processed timestamp to: $latestProcessedInThisBatch")
                }

                Log.d(TAG, "📊 Total new notifications processed: $newNotificationsCount")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Firebase Error: ${error.message}")
                error.toException().printStackTrace()
            }
        }

        globalBroadcastQuery = globalRef
        globalBroadcastQuery!!.addValueEventListener(globalBroadcastListener!!)

        Log.d(TAG, "👂 Now listening for broadcasts at: notifications/global")
    }

    private fun removeOldListener() {
        if (globalBroadcastListener != null) {
            globalBroadcastQuery?.removeEventListener(globalBroadcastListener!!)
            Log.d(TAG, "🔇 Removed old broadcast listener")
        }
        globalBroadcastListener = null
        globalBroadcastQuery = null
    }

    private fun saveNotificationToHistory(userId: String, title: String, message: String) {
        val data = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "read" to false
        )

        // Saves to /notifications/{userId}/{pushId}
        database.getReference("notifications")
            .child(userId)
            .push()
            .setValue(data)
            .addOnSuccessListener {
                Log.d(TAG, "💾 Saved notification to history for user: $userId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Failed to save notification history: ${exception.message}")
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BusMate Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Keeps BusMate running to receive announcements"
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            Log.d(TAG, "📢 Notification channel created")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🛑 BroadcastNotificationService DESTROYED")
        removeOldListener()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}