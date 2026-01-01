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

    // Listeners and State Tracking
    private var busQueries = mutableMapOf<String, Query>()
    private var busListeners = mutableMapOf<String, ValueEventListener>()
    private var lastStatus = mutableMapOf<String, Boolean>()

    private var adminNotificationQuery: Query? = null
    private var adminNotificationListener: ValueEventListener? = null

    // To prevent firing notifications for old database records on start
    private val serviceStartTime = System.currentTimeMillis()

    companion object {
        private const val CHANNEL_ID = "monitoring_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Required Foreground Notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BusMate Active")
            .setContentText("Monitoring bus activities...")
            .setSmallIcon(R.drawable.outline_directions_bus_24)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            identifyUserRoleAndStart(currentUserUid)
        }

        return START_STICKY
    }

    private fun identifyUserRoleAndStart(uid: String) {
        database.getReference("users").child(uid).get().addOnSuccessListener { snapshot ->
            val userType = snapshot.child("typeofUser").getValue(String::class.java)

            when (userType) {
                "Admin" -> {
                    Log.d("TripMonitor", "Role: Admin. Initializing Global Listeners.")
                    setupAdminNotificationListener() // Speed Alerts
                    setupAdminBusStatusListener()    // Trip Start/End for all buses
                }
                "Parent" -> {
                    Log.d("TripMonitor", "Role: Parent. Initializing Child Bus Listener.")
                    setupParentBusListeners(uid)     // Trip Start/End for specific child
                }
            }
        }
    }

    /**
     * ADMIN: Listens for Speed Alerts in notifications/admin
     */
    private fun setupAdminNotificationListener() {
        val adminRef = database.getReference("notifications").child("admin")

        // We look at the latest alert
        adminNotificationQuery = adminRef.limitToLast(1)
        adminNotificationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (alert in snapshot.children) {
                    val timestamp = alert.child("timestamp").getValue(Long::class.java) ?: 0L

                    // Only trigger if this notification happened after the service started
                    if (timestamp > serviceStartTime) {
                        val title = alert.child("title").getValue(String::class.java) ?: "Alert"
                        val message = alert.child("message").getValue(String::class.java) ?: ""
                        NotificationHelper.showNotification(applicationContext, title, message)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        adminNotificationQuery?.addValueEventListener(adminNotificationListener!!)
    }

    /**
     * ADMIN: Listens for Trip Start/End for ALL buses
     */
    private fun setupAdminBusStatusListener() {
        val busesRef = database.getReference("buses")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { busSnapshot ->
                    val busId = busSnapshot.key ?: return@forEach
                    val routeNo = busSnapshot.child("routeNo").getValue(String::class.java) ?: "N/A"
                    val isRunning = busSnapshot.child("isTripRunning").getValue(Boolean::class.java) ?: false

                    val prevStatus = lastStatus[busId]

                    // Trigger only on status CHANGE
                    if (prevStatus != null && prevStatus != isRunning) {
                        val title = if (isRunning) "Trip Started" else "Trip Ended"
                        val msg = "Bus Route $routeNo has ${if (isRunning) "started" else "ended"} its trip."

                        NotificationHelper.showNotification(applicationContext, title, msg)
                    }
                    lastStatus[busId] = isRunning
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        busesRef.addValueEventListener(listener)
        busQueries["admin_global_buses"] = busesRef
        busListeners["admin_global_buses"] = listener
    }

    /**
     * PARENT: Listens ONLY to the bus associated with their children
     */
    private fun setupParentBusListeners(uid: String) {
        val childrenRef = database.getReference("users").child(uid).child("children")
        childrenRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val routeId = childSnapshot.child("busRouteId").getValue(String::class.java)
                    val childName = childSnapshot.child("firstName").getValue(String::class.java) ?: "Child"

                    if (routeId != null && !busQueries.containsKey(routeId)) {
                        val busQuery = database.getReference("buses").child(routeId)
                        val listener = object : ValueEventListener {
                            override fun onDataChange(busSnapshot: DataSnapshot) {
                                val isRunning = busSnapshot.child("isTripRunning").getValue(Boolean::class.java) ?: false
                                val prevStatus = lastStatus[routeId]

                                if (prevStatus != null && prevStatus != isRunning) {
                                    val title = if (isRunning) "Trip Started" else "Trip Ended"
                                    val msg = if (isRunning) "$childName's bus is starting!" else "$childName's bus has finished the trip."

                                    NotificationHelper.showNotification(applicationContext, title, msg)
                                    // Parents keep their own local history
                                    saveNotificationToHistory(uid, title, msg)
                                }
                                lastStatus[routeId] = isRunning
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        }
                        busQuery.addValueEventListener(listener)
                        busQueries[routeId] = busQuery
                        busListeners[routeId] = listener
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveNotificationToHistory(userId: String, title: String, message: String) {
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
        adminNotificationListener?.let { adminNotificationQuery?.removeEventListener(it) }
        super.onDestroy()
    }
}