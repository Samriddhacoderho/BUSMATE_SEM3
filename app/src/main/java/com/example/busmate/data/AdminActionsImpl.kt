package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.example.busmate.model.UserModel
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot

class AdminActionsImpl : AdminActionsInterface {

    private val db = FirebaseDatabase.getInstance()
    private val usersRef = db.getReference("users")
    private val adminRef = db.getReference("user")
    private val busesRef = db.getReference("buses")

    override fun getUserbyID(
        userID: String,
        callback: (Boolean, UserModel?) -> Unit
    ) {
        usersRef.orderByChild("schoolId").equalTo(userID)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, null)
                        return
                    }

                    val user = snapshot.children.first()
                        .getValue(UserModel::class.java)

                    callback(user != null, user)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, null)
                }
            })
    }

    override fun deactivateUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        usersRef.orderByChild("schoolId").equalTo(userID)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "User not found")
                        return
                    }

                    snapshot.children.first().ref
                        .child("status")
                        .setValue("deactivated")
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                callback(true, "User Deactivated")
                            else
                                callback(false, "User Not Deactivated")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun reactivateUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        usersRef.orderByChild("schoolId").equalTo(userID)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "User not found")
                        return
                    }

                    snapshot.children.first().ref
                        .child("status")
                        .setValue("active")
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                callback(true, "User Reactivated")
                            else
                                callback(false, "User Not Reactivated")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun deleteUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        usersRef.orderByChild("schoolId").equalTo(userID)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "User not found")
                        return
                    }

                    val userSnapshot = snapshot.children.first()
                    val userModel = userSnapshot.getValue(UserModel::class.java)
                    val studentIdIndexRef = db.getReference("studentIdIndex")

                    // 1. Get the list of child IDs (studentIds) associated with this user
                    val childIds = userModel?.children?.keys ?: emptySet()

                    // 2. Remove each child from the studentIdIndex collection
                    childIds.forEach { studentId ->
                        studentIdIndexRef.child(studentId).removeValue()
                            .addOnFailureListener {
                                Log.e("AdminActions", "Failed to delete studentIdIndex for $studentId")
                            }
                    }

                    // 3. Delete the user from 'users' node
                    userSnapshot.ref.removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // 4. Delete the pre-created ID from 'user' node
                                adminRef.child(userID).removeValue()
                                callback(true, "User and associated student indices deleted")
                            } else {
                                callback(false, "User Not Deleted")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun getAllBus(callback: (Boolean, List<BusModel>?) -> Unit) {

        busesRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val buses = mutableListOf<BusModel>()

                for (child in snapshot.children) {

                    val bus = BusModel(
                        uid = child.key ?: "",
                        busNumber = child.child("busNumber")
                            .getValue(String::class.java) ?: "",
                        licensePlate = child.child("licensePlate")
                            .getValue(String::class.java) ?: "",
                        routeId = child.child("routeId")
                            .getValue(String::class.java) ?: "",
                        capacity = child.child("capacity")
                            .getValue(Int::class.java) ?: 0,
                        maintenanceStatus = child.child("maintenanceStatus")
                            .getValue(String::class.java) ?: "Good",
                        currentLocation = child.child("currentLocation")
                            .getValue(String::class.java) ?: "Depot",
                        speed = child.child("speed")
                            .getValue(Double::class.java) ?: 0.0,
                        // ✅ SAFE: driver may or may not exist
                        driver = child.child("driver")
                            .getValue(UserModel::class.java),
                        busImage = child.child("busImage").getValue(String::class.java).orEmpty()
                    )
                    buses.add(bus)
                }
                callback(true, buses)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, null)
            }
        })
    }
    override fun getAllDrivers(
        callback: (Boolean, List<UserModel>?) -> Unit
    ) {
        usersRef.orderByChild("typeofUser").equalTo("Driver")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val drivers = snapshot.children.mapNotNull {
                        it.getValue(UserModel::class.java)
                    }
                    callback(true, drivers)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false, null)
                }
            })
    }
    override fun assignBusToDriver(
        busId: String,
        driverId: String,
        callback: (Boolean, String) -> Unit
    ) {
        usersRef.orderByChild("schoolId").equalTo(driverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(driverSnap: DataSnapshot) {
                    val driver = driverSnap.children.firstOrNull()?.getValue(UserModel::class.java)
                    if (driver == null) {
                        callback(false, "Driver Not Found")
                        return
                    }

                    busesRef.child(busId).child("driver").setValue(driver)
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                callback(true, "Driver Assigned Successfully")
                            else
                                callback(false, "Failed to Assign Driver")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }
    // Add to AdminActionsImpl.kt
    // In AdminActionsImpl.kt
    override fun sendGlobalBroadcast(title: String, message: String, callback: (Boolean, String) -> Unit) {
        val broadcastRef = db.getReference("notifications/global")
        val id = broadcastRef.push().key ?: return

        val data = mapOf(
            "id" to id,
            "title" to title.trim(),
            "message" to message.trim(),
            "timestamp" to System.currentTimeMillis(),
        )

        broadcastRef.child(id).setValue(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Broadcast Sent Successfully")
            } else {
                callback(false, "Failed: ${task.exception?.message}")
            }
        }
    }
}
//testing
