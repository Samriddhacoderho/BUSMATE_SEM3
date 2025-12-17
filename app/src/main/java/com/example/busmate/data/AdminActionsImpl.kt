//package com.example.busmate.data
//
//import android.util.Log
//import com.example.busmate.model.BusModel
//import com.example.busmate.model.UserModel
//import com.google.firebase.firestore.FirebaseFirestore
//
//class AdminActionsImpl : AdminActionsInterface {
//    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
//    override fun getUserbyID(
//        userID: String,
//        callback: (Boolean, UserModel?) -> Unit
//    ) {
//        firestore.collection("users").whereEqualTo("schoolId", userID).get().addOnCompleteListener {
//            if (it.isSuccessful) {
//                val user = it.result.documents.firstOrNull()?.toObject(UserModel::class.java)
//                if (user != null) {
//                    callback(true, user)
//                } else {
//                    callback(false, null)
//                }
//            } else {
//                callback(false, null)
//            }
//        }
//    }
//    override fun deactivateUser(
//        userID: String,
//        callback: (Boolean, String) -> Unit
//    ) {
//        firestore.collection("users")
//            .whereEqualTo("schoolId", userID)
//            .get()
//            .addOnCompleteListener {
//
//                if (it.isSuccessful) {
//                    val doc = it.result.documents.firstOrNull()
//
//                    if (doc != null) {
//                        firestore.collection("users")
//                            .document(doc.id)   // correct Firestore document ID
//                            .update("status", "deactivated")
//                            .addOnCompleteListener { update ->
//                                if (update.isSuccessful) {
//                                    callback(true, "User Deactivated")
//                                } else {
//                                    callback(false, "User Not deactivated")
//                                }
//                            }
//                    } else {
//                        callback(false, "User not found")
//                    }
//
//                } else {
//                    callback(false, "Error fetching user")
//                }
//            }
//    }
//
//
//    override fun deleteUser(
//        userID: String,
//        callback: (Boolean, String) -> Unit
//    ) {
//        firestore.collection("users").whereEqualTo("schoolId",userID).get().addOnCompleteListener {
//            if(it.isSuccessful){
//                val doc = it.result.documents.firstOrNull()
//                if(doc!=null){
//                    firestore.collection("users").document(doc.id).delete().addOnCompleteListener {
//                        firestore.collection("user").document(userID).delete().addOnCompleteListener {
//                            if(it.isSuccessful) {
//                                callback(true, "User Deleted")
//                            }else{
//                                callback(false,"User Not Deleted")
//                            }
//                        }
//                    }
//                }else{
//                    callback(false,"User not found")
//                }
//            }else{
//                callback(false,"Error fetching user")
//            }
//        }
//    }
//
//
//    override fun reactivateUser(
//        userID: String,
//        callback: (Boolean, String) -> Unit
//    ) {
//        firestore.collection("users")
//            .whereEqualTo("schoolId", userID)
//            .get()
//            .addOnCompleteListener {
//
//                if (it.isSuccessful) {
//                    val doc = it.result.documents.firstOrNull()
//
//                    if (doc != null) {
//                        firestore.collection("users")
//                            .document(doc.id)   // correct Firestore document ID
//                            .update("status", "active")
//                            .addOnCompleteListener { update ->
//                                if (update.isSuccessful) {
//                                    callback(true, "User Reactivated")
//                                } else {
//                                    callback(false, "User Not Reactivated")
//                                }
//                            }
//                    } else {
//                        callback(false, "User not found")
//                    }
//
//                } else {
//                    callback(false, "Error fetching user")
//                }
//            }
//    }
//
//    override fun getAllBus(callback: (Boolean, List<BusModel>?) -> Unit) {
//
//        firestore.collection("buses")
//            .get()
//            .addOnCompleteListener { task ->
//
//                if (task.isSuccessful) {
//
//                    // Convert to your model
//                    val buses = task.result?.documents?.mapNotNull { doc ->
//                        doc.toObject(BusModel::class.java)
//                    }
//
//                    // 🟢 Print everything in Logcat
//                    Log.d("BusRepo", "Fetched ${buses?.size ?: 0} buses:")
//                    buses?.forEach { bus ->
//                        Log.d("BusRepo", bus.toString())
//                    }
//
//                    callback(true, buses)
//
//                } else {
//
//                    // 🔴 Print error
//                    Log.e("BusRepo", "Failed to fetch buses", task.exception)
//                    callback(false, null)
//                }
//            }
//    }
//
//    override fun getAllDrivers(callback: (Boolean, List<UserModel>?) -> Unit) {
//        firestore.collection("users").whereEqualTo("typeofUser","Driver")
//            .get()
//            .addOnCompleteListener { task ->
//
//                if (task.isSuccessful) {
//
//                    // Convert to your model
//                    val users = task.result?.documents?.mapNotNull { doc ->
//                        doc.toObject(UserModel::class.java)
//                    }
//
//                    callback(true, users)
//
//                } else {
//
//                    // 🔴 Print error
//                    Log.e("BusRepo", "Failed to fetch buses", task.exception)
//                    callback(false, null)
//                }
//            }
//    }
//
//    override fun assignBusToDriver(
//        busId: String,
//        driverId: String,
//        callback: (Boolean, String) -> Unit
//    ) {
//        val driversRef = firestore.collection("users")   // or "drivers"
//        val busesRef = firestore.collection("buses")
//
//        // 1️⃣ Get the driver model
//        driversRef.whereEqualTo("schoolId", driverId)
//            .get()
//            .addOnSuccessListener { driverSnapshot ->
//
//                val driverDoc = driverSnapshot.documents.firstOrNull()
//                if (driverDoc != null) {
//                    val driverModel = driverDoc.data
//
//                    // 2️⃣ Get the bus
//                    busesRef.whereEqualTo("uid", busId)
//                        .get()
//                        .addOnSuccessListener { busSnapshot ->
//
//                            val busDoc = busSnapshot.documents.firstOrNull()
//                            if (busDoc != null) {
//                                // 3️⃣ Update bus.driver with full driver model
//                                busesRef.document(busDoc.id)
//                                    .update("driver", driverModel)
//                                    .addOnSuccessListener {
//                                        callback(true, "Driver Assigned Successfully")
//                                    }
//                                    .addOnFailureListener {
//                                        callback(false, "Failed to assign driver")
//                                    }
//                            } else {
//                                callback(false, "Bus Not Found")
//                            }
//
//                        }
//                        .addOnFailureListener {
//                            callback(false, "Error fetching bus")
//                        }
//
//                } else {
//                    callback(false, "Driver Not Found")
//                }
//
//            }
//            .addOnFailureListener {
//                callback(false, "Error fetching driver")
//            }
//    }
//
//
//
//
//
//
//}

package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.example.busmate.model.UserModel
import com.google.firebase.database.*

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

                    snapshot.children.first().ref
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                adminRef.child(userID).removeValue()
                                callback(true, "User Deleted")
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
                            .getValue(UserModel::class.java)
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

}
