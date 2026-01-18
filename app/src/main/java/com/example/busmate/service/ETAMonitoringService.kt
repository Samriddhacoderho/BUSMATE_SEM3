package com.example.busmate.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.busmate.R
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.model.ChildModel
import com.example.busmate.util.NotificationHelper
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*

class ETAMonitoringService : Service() {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val busRepo = BusRepositoryImpl()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track which children have already received notifications
    private val notifiedChildren = mutableSetOf<String>()

    // Store listeners for cleanup
    private val busListeners = mutableMapOf<String, ValueEventListener>()

    // === NEW: Speed buffering like LocationViewModel ===
    private val speedBuffers = mutableMapOf<String, MutableList<Float>>()
    private val BUFFER_SIZE = 10

    companion object {
        private const val CHANNEL_ID = "eta_monitoring_channel"
        private const val NOTIFICATION_ID = 102
        private const val ETA_THRESHOLD_MINUTES = 5
        private const val CHECK_INTERVAL_MS = 30000L // Check every 30 seconds
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("ETAService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BusMate ETA Tracking")
            .setContentText("Monitoring bus arrival times...")
            .setSmallIcon(R.drawable.outline_directions_bus_24)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            Log.d("ETAService", "Starting ETA monitoring for parent: $currentUserUid")
            startETAMonitoring(currentUserUid)
        } else {
            Log.e("ETAService", "No authenticated user found")
        }

        return START_STICKY
    }

    private fun startETAMonitoring(parentUid: String) {
        database.getReference("users").child(parentUid).child("children")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ETAService", "Children snapshot exists: ${snapshot.exists()}")

                    val children = snapshot.children.mapNotNull {
                        it.getValue(ChildModel::class.java)
                    }

                    if (children.isEmpty()) {
                        Log.w("ETAService", "No children found for parent")
                        return
                    }

                    Log.d("ETAService", "Found ${children.size} children")

                    // === NEW: Log each child's details ===
                    children.forEach { child ->
                        Log.d("ETAService", "Child: ${child.firstName} ${child.lastName}")
                        Log.d("ETAService", "  - Student ID: ${child.studentId}")
                        Log.d("ETAService", "  - Bus Route ID: ${child.busRouteId}")
                        Log.d("ETAService", "  - Pickup: ${child.pickUpLat}, ${child.pickUpLng}")
                        Log.d("ETAService", "  - Dropoff: ${child.dropOffLat}, ${child.dropOffLng}")
                    }

                    // Group children by bus route
                    val childrenByBus = children.groupBy { it.busRouteId }

                    childrenByBus.forEach { (routeId, childList) ->
                        Log.d("ETAService", "=== BUS MONITORING SETUP ===")
                        Log.d("ETAService", "Bus Route ID from children: $routeId")
                        Log.d("ETAService", "Children count: ${childList.size}")
                        Log.d("ETAService", "Children names: ${childList.map { it.firstName }}")

                        if (routeId.isNotEmpty()) {
                            // CRITICAL FIX: Find the actual bus node using routeId
                            findAndMonitorBus(routeId, childList, parentUid)
                        } else {
                            Log.e("ETAService", "Empty routeId for children: ${childList.map { it.firstName }}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ETAService", "Failed to load children: ${error.message}")
                }
            })
    }

    /**
     * CRITICAL FIX: This method finds the actual bus node by querying with routeId
     * Children have busRouteId (e.g., "1010"), but buses might be stored by UID
     */
    private fun findAndMonitorBus(
        routeId: String,
        children: List<ChildModel>,
        parentUid: String
    ) {
        // Try direct path first (in case routeId IS the bus key)
        database.getReference("buses").child(routeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(directSnapshot: DataSnapshot) {
                    if (directSnapshot.exists()) {
                        Log.d("ETAService", "✓ Found bus at direct path: buses/$routeId")
                        monitorBusForChildren(routeId, children, parentUid)
                    } else {
                        // If not found at direct path, query by routeId field
                        Log.d("ETAService", "Bus not at direct path, querying by routeId field...")
                        database.getReference("buses")
                            .orderByChild("routeId")
                            .equalTo(routeId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(querySnapshot: DataSnapshot) {
                                    if (querySnapshot.exists()) {
                                        val busNode = querySnapshot.children.first()
                                        val actualBusKey = busNode.key
                                        Log.d("ETAService", "✓ Found bus via query: routeId=$routeId -> busKey=$actualBusKey")

                                        if (actualBusKey != null) {
                                            monitorBusForChildren(actualBusKey, children, parentUid)
                                        }
                                    } else {
                                        Log.e("ETAService", "✗ No bus found with routeId: $routeId")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ETAService", "Bus query failed: ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ETAService", "Direct bus check failed: ${error.message}")
                }
            })
    }

    private fun monitorBusForChildren(
        busId: String,
        children: List<ChildModel>,
        parentUid: String
    ) {
        Log.d("ETAService", "Setting up listener for bus: $busId")
        val busRef = database.getReference("buses").child(busId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ETAService", "--- Bus Data Update ---")
                Log.d("ETAService", "Bus snapshot exists: ${snapshot.exists()}")
                Log.d("ETAService", "Bus ID: $busId")

                if (!snapshot.exists()) {
                    Log.e("ETAService", "Bus node does not exist!")
                    return
                }

                Log.d("ETAService", "Bus fields: ${snapshot.children.map { it.key }}")

                val isTripRunning = snapshot.child("isTripRunning")
                    .getValue(Boolean::class.java) ?: false

                Log.d("ETAService", "isTripRunning: $isTripRunning")

                if (!isTripRunning) {
                    Log.w("ETAService", "Trip not running for bus $busId - skipping ETA calculation")
                    // Reset notifications when trip ends
                    children.forEach { child ->
                        notifiedChildren.remove(child.studentId)
                        clearNotificationState(child.studentId)
                    }
                    // === NEW: Clear speed buffer for this bus ===
                    speedBuffers.remove(busId)
                    return
                }

                val currentLocation = snapshot.child("currentLocation")
                    .getValue(String::class.java)

                if (currentLocation == null) {
                    Log.e("ETAService", "Current location is null for bus $busId")
                    return
                }

                Log.d("ETAService", "Current location: $currentLocation")

                val speed = snapshot.child("speed")
                    .getValue(Double::class.java)?.toFloat() ?: 0f

                val tripType = snapshot.child("tripType")
                    .getValue(String::class.java) ?: "Pickup"

                Log.d("ETAService", "Speed: $speed m/s (${speed * 3.6} km/h)")
                Log.d("ETAService", "Trip Type: $tripType")

                // Calculate ETA for each child
                children.forEach { child ->
                    calculateAndNotifyETA(
                        child = child,
                        currentLocation = currentLocation,
                        speed = speed,
                        tripType = tripType,
                        parentUid = parentUid,
                        busId = busId  // === NEW: Pass busId for speed buffering ===
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ETAService", "Bus monitoring error: ${error.message}")
            }
        }

        busRef.addValueEventListener(listener)
        busListeners[busId] = listener
        Log.d("ETAService", "Listener attached to bus: $busId")
    }

    private fun calculateAndNotifyETA(
        child: ChildModel,
        currentLocation: String,
        speed: Float,
        tripType: String,
        parentUid: String,
        busId: String  // === NEW: Added parameter ===
    ) {
        // Parse bus location
        val busLatLng = parseCoordinates(currentLocation)

        // Get destination based on trip type
        val destinationLatLng = if (tripType == "Pickup") {
            LatLng(child.pickUpLat, child.pickUpLng)
        } else {
            LatLng(child.dropOffLat, child.dropOffLng)
        }

        // === NEW: Use buffered speed like LocationViewModel ===
        // Get or create buffer for this bus
        val buffer = speedBuffers.getOrPut(busId) { mutableListOf() }

        // Add current speed to buffer
        if (buffer.size >= BUFFER_SIZE) {
            buffer.removeAt(0)
        }
        buffer.add(speed)

        // Calculate average speed from buffer
        val avgSpeedMps = if (buffer.isNotEmpty()) {
            buffer.average().toFloat()
        } else {
            0f
        }

        // Calculate distance
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            busLatLng.latitude, busLatLng.longitude,
            destinationLatLng.latitude, destinationLatLng.longitude,
            results
        )
        val distanceMeters = results[0]

        // === UPDATED: Use same default speed as LocationViewModel (5.5 m/s) ===
        val effectiveSpeed = if (avgSpeedMps < 0.5f) {
            Log.w("ETAService", "Low/zero speed detected for ${child.firstName}, using default 5.5 m/s (~20 km/h)")
            5.5f // CHANGED from 8.33 to match LocationViewModel
        } else {
            avgSpeedMps
        }

        val etaMinutes = (distanceMeters / (effectiveSpeed * 60)).toInt()

        Log.d("ETAService", "ETA Calculation for ${child.firstName}:")
        Log.d("ETAService", "  - Distance: ${distanceMeters.toInt()}m")
        Log.d("ETAService", "  - Raw Speed: $speed m/s")
        Log.d("ETAService", "  - Buffered Speed: $avgSpeedMps m/s")
        Log.d("ETAService", "  - Effective Speed: $effectiveSpeed m/s")
        Log.d("ETAService", "  - ETA: $etaMinutes minutes")
        Log.d("ETAService", "  - Threshold: $ETA_THRESHOLD_MINUTES minutes")
        Log.d("ETAService", "  - Already notified: ${notifiedChildren.contains(child.studentId)}")

        // === NEW: Check both in-memory and persistent notification state ===
        if (etaMinutes <= ETA_THRESHOLD_MINUTES &&
            etaMinutes > 0 &&
            !notifiedChildren.contains(child.studentId) &&
            !wasRecentlyNotified(child.studentId)) {

            Log.d("ETAService", "🔔 SENDING NOTIFICATION for ${child.firstName}")
            sendETANotification(child, etaMinutes, tripType, parentUid)
            notifiedChildren.add(child.studentId)
            saveNotificationState(child.studentId)  // === NEW: Persist state ===

            // Remove from both sets after 10 minutes
            serviceScope.launch {
                delay(600000L) // 10 minutes
                notifiedChildren.remove(child.studentId)
                clearNotificationState(child.studentId)  // === NEW: Clear persistent state ===
                Log.d("ETAService", "Reset notification flag for ${child.firstName}")
            }
        } else {
            if (etaMinutes > ETA_THRESHOLD_MINUTES) {
                Log.d("ETAService", "ETA ($etaMinutes min) > threshold ($ETA_THRESHOLD_MINUTES min) - not sending notification")
            } else if (etaMinutes <= 0) {
                Log.d("ETAService", "ETA is 0 or negative - bus may have passed")
            } else if (notifiedChildren.contains(child.studentId) || wasRecentlyNotified(child.studentId)) {
                Log.d("ETAService", "Already notified for ${child.firstName} - skipping")
            }
        }
    }

    private fun sendETANotification(
        child: ChildModel,
        etaMinutes: Int,
        tripType: String,
        parentUid: String
    ) {
        val title = if (etaMinutes <= 1) {
            "${child.firstName}'s bus is arriving now!"
        } else {
            "${child.firstName}'s bus arriving soon"
        }

        val message = if (tripType == "Pickup") {
            "The bus will reach ${child.firstName}'s pickup location in $etaMinutes minute(s)"
        } else {
            "The bus will reach ${child.firstName}'s drop-off location in $etaMinutes minute(s)"
        }

        Log.d("ETAService", "Notification content: $title - $message")

        // Show system notification
        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = message
        )

        // Save to Firebase for in-app notification history
        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "type" to "eta_alert",
            "childId" to child.studentId
        )

        database.getReference("notifications")
            .child(parentUid)
            .push()
            .setValue(notificationData)
            .addOnSuccessListener {
                Log.d("ETAService", "✓ Notification saved to Firebase for ${child.firstName}")
            }
            .addOnFailureListener { e ->
                Log.e("ETAService", "✗ Failed to save notification: ${e.message}")
            }
    }

    // === NEW: SharedPreferences methods to prevent duplicate notifications ===
    private fun saveNotificationState(childId: String) {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("notified_$childId", System.currentTimeMillis())
            .apply()
        Log.d("ETAService", "Saved notification state for child: $childId")
    }

    private fun wasRecentlyNotified(childId: String): Boolean {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        val lastNotified = prefs.getLong("notified_$childId", 0L)
        val timeSince = System.currentTimeMillis() - lastNotified
        val wasNotified = timeSince < 600000L // 10 minutes

        if (wasNotified) {
            Log.d("ETAService", "Child $childId was notified ${timeSince / 1000}s ago")
        }

        return wasNotified
    }

    private fun clearNotificationState(childId: String) {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        prefs.edit().remove("notified_$childId").apply()
        Log.d("ETAService", "Cleared notification state for child: $childId")
    }

    private fun parseCoordinates(coords: String): LatLng {
        return try {
            val parts = coords.split(",")
            val lat = parts[0].trim().toDouble()
            val lng = parts[1].trim().toDouble()
            LatLng(lat, lng)
        } catch (e: Exception) {
            Log.e("ETAService", "Failed to parse coordinates: $coords - ${e.message}")
            LatLng(27.7172, 85.3240) // Default location (Kathmandu)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ETA Monitoring",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for bus arrival times"
                enableVibration(true)
                enableLights(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            Log.d("ETAService", "Notification channel created")
        }
    }

    override fun onDestroy() {
        Log.d("ETAService", "Service destroyed - cleaning up ${busListeners.size} listeners")
        // Clean up listeners
        busListeners.forEach { (busId, listener) ->
            database.getReference("buses").child(busId).removeEventListener(listener)
            Log.d("ETAService", "Removed listener for bus: $busId")
        }
        // === NEW: Clear speed buffers ===
        speedBuffers.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}