package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.google.firebase.database.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.net.URL

class BusRepositoryImpl : BusRepositoryInterface {

    private val db = FirebaseDatabase.getInstance()
    private val busesRef = db.getReference("buses")

    override fun updateLocationByDriver(driverUid: String, latLng: LatLng) {
        // Query: Look into "buses" where the nested "driver/uid" matches our driverUid
        busesRef.orderByChild("driver/uid").equalTo(driverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Firebase returns a Map; we take the first (and only) bus matching this driver
                        val busSnapshot = snapshot.children.first()
                        val busUid = busSnapshot.key ?: return

                        // Convert LatLng to String "latitude,longitude" to match BusModel
                        val locationString = "${latLng.latitude},${latLng.longitude}"


                        // Update the specific bus's currentLocation field
                        busesRef.child(busUid).child("currentLocation").setValue(locationString)
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Failed to update location: ${e.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Query cancelled: ${error.message}")
                }
            })
    }

    override fun registerBus(
        bus: BusModel,
        callback: (String, Boolean) -> Unit
    ) {
        val busNumber = bus.busNumber.trim()
        val licensePlate = bus.licensePlate.trim()

        if (busNumber.isBlank() || licensePlate.isBlank()) {
            callback("Missing required fields (Bus Number or License Plate)", false)
            return
        }

        // 1️⃣ Check unique Bus Number
        busesRef.orderByChild("busNumber").equalTo(busNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(busNumberSnap: DataSnapshot) {

                    if (busNumberSnap.exists()) {
                        callback("Bus Number '$busNumber' already exists.", false)
                        return
                    }

                    // 2️⃣ Check unique License Plate
                    busesRef.orderByChild("licensePlate").equalTo(licensePlate)
                        .addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onDataChange(licenseSnap: DataSnapshot) {

                                if (licenseSnap.exists()) {
                                    callback(
                                        "License Plate '$licensePlate' is already assigned to another bus.",
                                        false
                                    )
                                    return
                                }

                                // 3️⃣ Generate Firebase UID
                                val newBusRef = busesRef.push()
                                val busUid = newBusRef.key!!

                                val updatedBus = bus.copy(
                                    uid = busUid,
                                    driver = null,
                                    busImage = bus.busImage
                                )
                                // 4️⃣ Save bus
                                newBusRef.setValue(updatedBus.toMap())
                                    .addOnCompleteListener {

                                        if (it.isSuccessful) {
                                            Log.d(
                                                "BusRepo",
                                                "Bus registered successfully: $busUid"
                                            )
                                            callback(
                                                "Bus $busNumber registered successfully!",
                                                true
                                            )
                                        } else {
                                            callback(
                                                it.exception?.message
                                                    ?: "Failed to register bus",
                                                false
                                            )
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(error.message, false)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(error.message, false)
                }
            })
    }
    override fun updateBusLocation(
        busUid: String,
        latLng: com.google.android.gms.maps.model.LatLng,
        callback: (Boolean) -> Unit
    ) {
        // Formatting LatLng as a string "lat,long" to match your BusModel's currentLocation type
        val locationString = "${latLng.latitude},${latLng.longitude}"

        // Target the specific bus by UID and update only the currentLocation field
        busesRef.child(busUid).child("currentLocation").setValue(locationString)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }
    // In BusRepositoryImpl.kt
    override fun getLiveBusLocation(busId: String, callback: (String) -> Unit) {
        // Reference the specific bus and its currentLocation field
        busesRef.child(busId).child("currentLocation")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val location = snapshot.getValue(String::class.java) ?: "0.0,0.0"
                    callback(location)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    override fun checkBusRouteExists(
        busRouteId: String,
        callback: (Boolean, String) -> Unit
    ) {
        busesRef
            .orderByChild("routeId")
            .equalTo(busRouteId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        callback(true, "Bus route linked successfully")
                    } else {
                        callback(false, "No bus found for route ID: $busRouteId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun getBusByRouteId(
        busRouteId: String,
        callback: (BusModel?) -> Unit
    ) {
        busesRef.orderByChild("routeId").equalTo(busRouteId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(null)
                        return
                    }

                    val busSnapshot = snapshot.children.first()
                    val bus = busSnapshot.getValue(BusModel::class.java)

                    // Get the driver's UID from the nested bus data
                    val driverUid = bus?.driver?.uid

                    if (driverUid != null) {
                        // GO TO THE USERS NODE TO GET THE REAL IMAGE
                        db.getReference("users").child(driverUid).get()
                            .addOnSuccessListener { userSnapshot ->
                                val latestImageUrl = userSnapshot.child("profileImage").getValue(String::class.java)

                                // Inject the live image URL into the driver object
                                if (!latestImageUrl.isNullOrEmpty()) {
                                    bus.driver = bus.driver?.copy(profileImage = latestImageUrl)
                                }
                                callback(bus)
                            }
                            .addOnFailureListener {
                                callback(bus) // Return bus even if user fetch fails
                            }
                    } else {
                        callback(bus)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    override fun getBusByDriverUid(driverUid: String, callback: (BusModel?) -> Unit) {
        // We query the "buses" node and look for the nested "driver/uid" field
        busesRef.orderByChild("driver/uid").equalTo(driverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the first bus that matches this driver
                        val bus = snapshot.children.first().getValue(BusModel::class.java)
                        callback(bus)
                    } else {
                        callback(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    override fun getAllBusesLive(callback: (List<BusModel?>) -> Unit) {
        busesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val busList = mutableListOf<BusModel>()
                for (childSnapshot in snapshot.children) {
                    val bus = childSnapshot.getValue(BusModel::class.java)
                    bus?.let { busList.add(it) }
                }
                callback(busList)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching all buses: ${error.message}")
            }
        })
    }

    override fun getRoadSnappedRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        onSuccess: (List<LatLng>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "key=$apiKey"

        Thread {
            val connection = URL(urlString).openConnection()
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val status = json.getString("status")

            if (status == "OK") {
                val routes = json.getJSONArray("routes")
                val points = routes.getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val decodedPath = PolyUtil.decode(points)
                onSuccess(decodedPath)
            } else {
                onFailure("Directions API Error: $status")
            }
        }.start()
    }


}
//testing the current location of driver