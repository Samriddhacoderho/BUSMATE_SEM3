package com.example.busmate.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.busmate.R
import com.example.busmate.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TripMonitoringService : Service() {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var busQueries = mutableMapOf<String, Query>()
    private var busListeners = mutableMapOf<String, ValueEventListener>()

    // Track the last known status to prevent notification spam
    private var lastStatus = mutableMapOf<String, Boolean>()

    companion object {
        private const val CHANNEL_ID = "monitoring_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Build the mandatory foreground notification required for Android 14+
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BusMate Monitoring Active")
            .setContentText("Checking for trip starts...")
            .setSmallIcon(R.drawable.outline_directions_bus_24)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        // Use try-catch to satisfy Android 14's strict foreground start rules
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("TripMonitor", "Foreground start failed: ${e.message}")
        }

        val busRouteIds = intent?.getStringArrayListExtra("BUS_ROUTE_IDS") ?: return START_NOT_STICKY
        Log.d("TripMonitor", "Monitoring routes: $busRouteIds")

        busRouteIds.forEach { routeId ->
            if (!busListeners.containsKey(routeId)) {
                // Query by "routeId" field inside the random bus nodes
                val busQuery = database.getReference("buses")
                    .orderByChild("routeId")
                    .equalTo(routeId)

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (busSnapshot in snapshot.children) {
                            val isRunning = busSnapshot.child("isTripRunning").getValue(Boolean::class.java) ?: false
                            val busNo = busSnapshot.child("busNumber").getValue(String::class.java) ?: routeId

                            val previousStatus = lastStatus[routeId] ?: false

                            // Only notify when the bus status CHANGES from false to true
                            if (isRunning && !previousStatus) {
                                val title = "Bus $busNo Started"
                                val message = "The bus for route $routeId has started its trip."

                                NotificationHelper.showNotification(applicationContext, title, message)
                                saveNotificationToHistory(title, message)

                                lastStatus[routeId] = true
                            } else if (!isRunning) {
                                // Reset status when trip ends so it can trigger again next time
                                lastStatus[routeId] = false
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("TripMonitor", "Query Error: ${error.message}")
                    }
                }

                busQuery.addValueEventListener(listener)
                busQueries[routeId] = busQuery
                busListeners[routeId] = listener
            }
        }
        return START_STICKY
    }

    private fun saveNotificationToHistory(title: String, message: String) {
        val userId = auth.currentUser?.uid ?: return
        val data = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "read" to false
        )
        database.getReference("notifications").child(userId).push().setValue(data)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Bus Monitoring", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        busQueries.forEach { (id, query) -> busListeners[id]?.let { query.removeEventListener(it) } }
        super.onDestroy()
    }
}