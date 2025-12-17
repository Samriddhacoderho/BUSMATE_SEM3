package com.example.busmate.data

import com.example.busmate.model.SupportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupportRepositoryImpl : SupportRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val supportRef = db.getReference("support")

    override fun writeSupport(
        model: SupportModel,
        callback: (Boolean, String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(false, "User not logged in")
            return
        }

        val updatedModel = model.copy(uid = uid)

        supportRef.child(uid)
            .setValue(updatedModel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Support request submitted")
                } else {
                    callback(false, it.exception?.message ?: "Failed to submit support")
                }
            }
    }

    override fun fetchSupportMessages(
        callback: (Boolean, String, List<SupportModel>) -> Unit
    ) {
        supportRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SupportModel>()

                for (child in snapshot.children) {
                    val support = child.getValue(SupportModel::class.java)
                    if (support != null) list.add(support)
                }

                callback(true, "Support messages loaded", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun replyToSupport(
        supportId: String,
        replyMessage: String,
        callback: (Boolean, String) -> Unit
    ) {
        supportRef.child(supportId)
            .child("reply")
            .setValue(replyMessage)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Reply sent successfully")
                } else {
                    callback(false, it.exception?.message ?: "Failed to send reply")
                }
            }
    }
}
