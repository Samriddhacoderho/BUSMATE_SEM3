package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.BusModel
import com.example.busmate.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore

class AdminActionsImpl : AdminActionsInterface {
    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun getUserbyID(
        userID: String,
        callback: (Boolean, UserModel?) -> Unit
    ) {
        firestore.collection("users").whereEqualTo("schoolId", userID).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result.documents.firstOrNull()?.toObject(UserModel::class.java)
                if (user != null) {
                    callback(true, user)
                } else {
                    callback(false, null)
                }
            } else {
                callback(false, null)
            }
        }
    }
    override fun deactivateUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("schoolId", userID)
            .get()
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    val doc = it.result.documents.firstOrNull()

                    if (doc != null) {
                        firestore.collection("users")
                            .document(doc.id)   // correct Firestore document ID
                            .update("status", "deactivated")
                            .addOnCompleteListener { update ->
                                if (update.isSuccessful) {
                                    callback(true, "User Deactivated")
                                } else {
                                    callback(false, "User Not deactivated")
                                }
                            }
                    } else {
                        callback(false, "User not found")
                    }

                } else {
                    callback(false, "Error fetching user")
                }
            }
    }


    override fun deleteUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        firestore.collection("users").whereEqualTo("schoolId",userID).get().addOnCompleteListener {
            if(it.isSuccessful){
                val doc = it.result.documents.firstOrNull()
                if(doc!=null){
                    firestore.collection("users").document(doc.id).delete().addOnCompleteListener {
                        firestore.collection("user").document(userID).delete().addOnCompleteListener {
                            if(it.isSuccessful) {
                                callback(true, "User Deleted")
                            }else{
                                callback(false,"User Not Deleted")
                            }
                        }
                    }
                }else{
                    callback(false,"User not found")
                }
            }else{
                callback(false,"Error fetching user")
            }
        }
    }


    override fun reactivateUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("schoolId", userID)
            .get()
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    val doc = it.result.documents.firstOrNull()

                    if (doc != null) {
                        firestore.collection("users")
                            .document(doc.id)   // correct Firestore document ID
                            .update("status", "active")
                            .addOnCompleteListener { update ->
                                if (update.isSuccessful) {
                                    callback(true, "User Reactivated")
                                } else {
                                    callback(false, "User Not Reactivated")
                                }
                            }
                    } else {
                        callback(false, "User not found")
                    }

                } else {
                    callback(false, "Error fetching user")
                }
            }
    }

    override fun getAllBus(callback: (Boolean, List<BusModel>?) -> Unit) {

        firestore.collection("buses")
            .get()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Convert to your model
                    val buses = task.result?.documents?.mapNotNull { doc ->
                        doc.toObject(BusModel::class.java)
                    }

                    // 🟢 Print everything in Logcat
                    Log.d("BusRepo", "Fetched ${buses?.size ?: 0} buses:")
                    buses?.forEach { bus ->
                        Log.d("BusRepo", bus.toString())
                    }

                    callback(true, buses)

                } else {

                    // 🔴 Print error
                    Log.e("BusRepo", "Failed to fetch buses", task.exception)
                    callback(false, null)
                }
            }
    }

    override fun getAllDrivers(callback: (Boolean, List<UserModel>?) -> Unit) {
        firestore.collection("users").whereEqualTo("typeofUser","Driver")
            .get()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // Convert to your model
                    val users = task.result?.documents?.mapNotNull { doc ->
                        doc.toObject(UserModel::class.java)
                    }

                    callback(true, users)

                } else {

                    // 🔴 Print error
                    Log.e("BusRepo", "Failed to fetch buses", task.exception)
                    callback(false, null)
                }
            }
    }



}