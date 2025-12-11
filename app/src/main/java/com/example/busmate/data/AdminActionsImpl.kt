package com.example.busmate.data

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
        TODO("Not yet implemented")
    }

    override fun deleteUser(
        userID: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}