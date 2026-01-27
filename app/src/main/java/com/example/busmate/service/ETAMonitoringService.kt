package com.example.busmate.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    // Speed buffering like LocationViewModel
    private val speedBuffers = mutableMapOf<String, MutableList<Float>>()
    private val BUFFER_SIZE = 10

    // Cache route distances per child
    private val routeDistanceCache = mutableMapOf<String, Int>()

    // === NEW: Drop-off state tracking ===
    data class DropOffState(
        var isNearDropOff: Boolean = false,
        var hasNotified: Boolean = false,
        var lastDistance: Float = Float.MAX_VALUE
    )
    private val dropOffStates = mutableMapOf<String, DropOffState>() // Key: studentId

    companion object {
        private const val CHANNEL_ID = "eta_monitoring_channel"
        private const val NOTIFICATION_ID = 102
        private const val ETA_THRESHOLD_MINUTES = 5
        private const val CHECK_INTERVAL_MS = 30000L

        // === NEW: Drop-off detection thresholds ===
        private const val DROP_OFF_RADIUS_METERS = 50f // Proximity zone
        private const val ARRIVAL_THRESHOLD_METERS = 25f // Actual arrival
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

    private fun findAndMonitorBus(
        routeId: String,
        children: List<ChildModel>,
        parentUid: String
    ) {
        database.getReference("buses").child(routeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(directSnapshot: DataSnapshot) {
                    if (directSnapshot.exists()) {
                        Log.d("ETAService", "✓ Found bus at direct path: buses/$routeId")
                        monitorBusForChildren(routeId, children, parentUid)
                    } else {
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
                    Log.w("ETAService", "Trip not running for bus $busId - skipping calculations")
                    // Clear drop-off states when trip ends
                    dropOffStates.clear()
                    // FIXED: Clear trip-specific notification states when trip ends
                    clearAllTripNotificationStates()
                    Log.d("ETAService", "🔄 Reset drop-off states and notification flags - trip ended")
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

                // Get API Key from manifest
                val apiKey = try {
                    packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    ).metaData.getString("com.google.android.geo.API_KEY") ?: ""
                } catch (e: Exception) {
                    Log.e("ETAService", "Failed to get API key: ${e.message}")
                    ""
                }

                // Process each child
                children.forEach { child ->
                    // ETA Calculation (existing)
                    calculateAndNotifyETA(
                        child = child,
                        currentLocation = currentLocation,
                        speed = speed,
                        tripType = tripType,
                        parentUid = parentUid,
                        busId = busId,
                        apiKey = apiKey
                    )

                    // === NEW: Drop-off Arrival Detection ===
                    checkDropOffArrival(
                        child = child,
                        currentLocation = currentLocation,
                        tripType = tripType,
                        parentUid = parentUid
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

    // === NEW: Drop-off arrival detection method ===
    private fun checkDropOffArrival(
        child: ChildModel,
        currentLocation: String,
        tripType: String,
        parentUid: String
    ) {
        // Only check during Drop-off trips
        if (tripType != "Drop-off") {
            return
        }

        val busLatLng = parseCoordinates(currentLocation)
        val dropOffLatLng = LatLng(child.dropOffLat, child.dropOffLng)

        // Calculate distance between bus and drop-off point
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            busLatLng.latitude, busLatLng.longitude,
            dropOffLatLng.latitude, dropOffLatLng.longitude,
            results
        )
        val distance = results[0]

        // Get or create state for this child
        val state = dropOffStates.getOrPut(child.studentId) { DropOffState() }

        Log.d("ETAService", "Drop-off check for ${child.firstName}: ${distance.toInt()}m away")

        when {
            // Bus just arrived at drop-off (within arrival threshold)
            distance <= ARRIVAL_THRESHOLD_METERS && !state.hasNotified -> {
                sendDropOffNotification(child, parentUid)
                state.hasNotified = true
                state.isNearDropOff = true
                Log.d("ETAService", "✅ Drop-off arrival detected for ${child.firstName}")
            }

            // Bus entering proximity zone
            distance <= DROP_OFF_RADIUS_METERS && !state.isNearDropOff -> {
                state.isNearDropOff = true
                Log.d("ETAService", "📍 Bus approaching ${child.firstName}'s drop-off (${distance.toInt()}m)")
            }

            // Bus leaving proximity zone
            distance > DROP_OFF_RADIUS_METERS && state.isNearDropOff -> {
                state.isNearDropOff = false
                // Don't reset hasNotified here - wait for trip end
                Log.d("ETAService", "🚌 Bus left ${child.firstName}'s drop-off area (${distance.toInt()}m)")
            }
        }

        state.lastDistance = distance
    }

    // === NEW: Send drop-off notification ===
    private fun sendDropOffNotification(
        child: ChildModel,
        parentUid: String
    ) {
        val title = "${child.firstName} has been dropped off"
        val message = "The bus has arrived at ${child.firstName}'s drop-off location."

        Log.d("ETAService", "📢 Sending drop-off notification: $title")

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = message
        )

        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "type" to "drop_off_arrival",
            "childId" to child.studentId
        )

        database.getReference("notifications")
            .child(parentUid)
            .push()
            .setValue(notificationData)
            .addOnSuccessListener {
                Log.d("ETAService", "✅ Drop-off notification saved to Firebase")
            }
            .addOnFailureListener { e ->
                Log.e("ETAService", "❌ Failed to save notification: ${e.message}")
            }
    }

    private fun calculateAndNotifyETA(
        child: ChildModel,
        currentLocation: String,
        speed: Float,
        tripType: String,
        parentUid: String,
        busId: String,
        apiKey: String
    ) {
        val busLatLng = parseCoordinates(currentLocation)

        val destinationLatLng = if (tripType == "Pickup") {
            LatLng(child.pickUpLat, child.pickUpLng)
        } else {
            LatLng(child.dropOffLat, child.dropOffLng)
        }

        // Fetch route distance using Google Maps API
        if (apiKey.isNotEmpty()) {
            busRepo.getRoadSnappedRoute(
                origin = busLatLng,
                destination = destinationLatLng,
                apiKey = apiKey,
                onSuccess = { _, distanceMeters ->
                    // Cache the route distance
                    routeDistanceCache[child.studentId] = distanceMeters
                    Log.d("ETAService", "✓ Route distance fetched: $distanceMeters m for ${child.firstName}")

                    // Now calculate ETA with the route distance
                    performETACalculation(child, speed, distanceMeters.toFloat(), tripType, parentUid, busId)
                },
                onFailure = { error ->
                    Log.e("ETAService", "Route fetch failed: $error - falling back to cached/aerial distance")

                    // Use cached distance if available, otherwise aerial
                    val cachedDistance = routeDistanceCache[child.studentId]
                    val distanceMeters = if (cachedDistance != null && cachedDistance > 0) {
                        Log.d("ETAService", "Using cached route distance: $cachedDistance m")
                        cachedDistance.toFloat()
                    } else {
                        // Fallback to aerial distance
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            busLatLng.latitude, busLatLng.longitude,
                            destinationLatLng.latitude, destinationLatLng.longitude,
                            results
                        )
                        Log.d("ETAService", "Using aerial distance: ${results[0].toInt()} m")
                        results[0]
                    }

                    performETACalculation(child, speed, distanceMeters, tripType, parentUid, busId)
                }
            )
        } else {
            Log.w("ETAService", "API key not available - using aerial distance")

            // Use cached distance or aerial as fallback
            val cachedDistance = routeDistanceCache[child.studentId]
            val distanceMeters = if (cachedDistance != null && cachedDistance > 0) {
                cachedDistance.toFloat()
            } else {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    busLatLng.latitude, busLatLng.longitude,
                    destinationLatLng.latitude, destinationLatLng.longitude,
                    results
                )
                results[0]
            }

            performETACalculation(child, speed, distanceMeters, tripType, parentUid, busId)
        }
    }

    private fun performETACalculation(
        child: ChildModel,
        speed: Float,
        distanceMeters: Float,
        tripType: String,
        parentUid: String,
        busId: String
    ) {
        // Speed buffering (same as LocationViewModel)
        val buffer = speedBuffers.getOrPut(busId) { mutableListOf() }

        if (buffer.size >= BUFFER_SIZE) {
            buffer.removeAt(0)
        }
        buffer.add(speed)

        val avgSpeedMps = if (buffer.isNotEmpty()) {
            buffer.average().toFloat()
        } else {
            0f
        }

        // Use same default speed as LocationViewModel (5.5 m/s)
        val effectiveSpeed = if (avgSpeedMps < 0.5f) {
            Log.w("ETAService", "Low/zero speed for ${child.firstName}, using default 5.5 m/s (~20 km/h)")
            5.5f
        } else {
            avgSpeedMps
        }

        val etaMinutes = (distanceMeters / (effectiveSpeed * 60)).toInt()

        Log.d("ETAService", "ETA Calculation for ${child.firstName}:")
        Log.d("ETAService", "  - Distance: ${distanceMeters.toInt()}m (${if (routeDistanceCache.containsKey(child.studentId)) "route" else "aerial"})")
        Log.d("ETAService", "  - Raw Speed: $speed m/s")
        Log.d("ETAService", "  - Buffered Speed: $avgSpeedMps m/s")
        Log.d("ETAService", "  - Effective Speed: $effectiveSpeed m/s")
        Log.d("ETAService", "  - ETA: $etaMinutes minutes")
        Log.d("ETAService", "  - Threshold: $ETA_THRESHOLD_MINUTES minutes")

        // FIXED: Check if trip is still running before sending notification
        database.getReference("buses").child(busId).child("isTripRunning")
            .get()
            .addOnSuccessListener { snapshot ->
                val isTripRunning = snapshot.getValue(Boolean::class.java) ?: false

                if (!isTripRunning) {
                    Log.d("ETAService", "⚠️ Trip ended - cancelling notification for ${child.firstName}")
                    return@addOnSuccessListener
                }

                // Check notification state per trip type
                if (etaMinutes <= ETA_THRESHOLD_MINUTES &&
                    etaMinutes > 0 &&
                    !wasRecentlyNotified(child.studentId, tripType)) {

                    Log.d("ETAService", "🔔 SENDING NOTIFICATION for ${child.firstName} ($tripType)")
                    sendETANotification(child, etaMinutes, tripType, parentUid)
                    notifiedChildren.add(child.studentId)
                    saveNotificationState(child.studentId, tripType)

                    serviceScope.launch {
                        delay(600000L) // 10 minutes
                        notifiedChildren.remove(child.studentId)
                        clearNotificationState(child.studentId, tripType)
                        Log.d("ETAService", "Reset notification flag for ${child.firstName} ($tripType)")
                    }
                } else {
                    if (etaMinutes > ETA_THRESHOLD_MINUTES) {
                        Log.d("ETAService", "ETA ($etaMinutes min) > threshold - not sending")
                    } else if (etaMinutes <= 0) {
                        Log.d("ETAService", "ETA is 0 or negative - bus may have passed")
                    } else if (wasRecentlyNotified(child.studentId, tripType)) {
                        Log.d("ETAService", "Already notified for ${child.firstName} ($tripType)")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ETAService", "Failed to check trip status: ${e.message}")
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

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = message
        )

        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to ServerValue.TIMESTAMP,
            "type" to "eta_alert",
            "childId" to child.studentId,
            "tripType" to tripType // FIXED: Store trip type with notification
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

    // FIXED: Save notification state per trip type
    private fun saveNotificationState(childId: String, tripType: String) {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        val key = "notified_${childId}_${tripType}" // Separate keys for Pickup vs Drop-off
        prefs.edit()
            .putLong(key, System.currentTimeMillis())
            .apply()
        Log.d("ETAService", "Saved notification state for child: $childId ($tripType)")
    }

    // FIXED: Check notification state per trip type
    private fun wasRecentlyNotified(childId: String, tripType: String): Boolean {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        val key = "notified_${childId}_${tripType}" // Separate keys for Pickup vs Drop-off
        val lastNotified = prefs.getLong(key, 0L)
        val timeSince = System.currentTimeMillis() - lastNotified
        val wasNotified = timeSince < 600000L // 10 minutes

        if (wasNotified) {
            Log.d("ETAService", "Child $childId was notified ${timeSince / 1000}s ago ($tripType)")
        }

        return wasNotified
    }

    // FIXED: Clear notification state per trip type
    private fun clearNotificationState(childId: String, tripType: String) {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        val key = "notified_${childId}_${tripType}"
        prefs.edit().remove(key).apply()
        Log.d("ETAService", "Cleared notification state for child: $childId ($tripType)")
    }

    // FIXED: NEW - Clear all trip notification states when trip ends
    private fun clearAllTripNotificationStates() {
        val prefs = getSharedPreferences("eta_notifications", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Clear all notification timestamps
        prefs.all.keys.forEach { key ->
            if (key.startsWith("notified_")) {
                editor.remove(key)
            }
        }

        editor.apply()
        notifiedChildren.clear()
        Log.d("ETAService", "🧹 Cleared all notification states for new trip")
    }

    private fun parseCoordinates(coords: String): LatLng {
        return try {
            val parts = coords.split(",")
            val lat = parts[0].trim().toDouble()
            val lng = parts[1].trim().toDouble()
            LatLng(lat, lng)
        } catch (e: Exception) {
            Log.e("ETAService", "Failed to parse coordinates: $coords - ${e.message}")
            LatLng(27.7172, 85.3240)
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
        busListeners.forEach { (busId, listener) ->
            database.getReference("buses").child(busId).removeEventListener(listener)
            Log.d("ETAService", "Removed listener for bus: $busId")
        }
        speedBuffers.clear()
        routeDistanceCache.clear()
        notifiedChildren.clear()
        dropOffStates.clear() // === NEW: Clear drop-off states ===
        clearAllTripNotificationStates() // FIXED: Clear all notification states
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}