package com.example.busmate.data

import com.example.busmate.model.SupportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupportRepositoryImpl : SupportRepositoryInterface {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val supportRef = database.getReference("support")

    override suspend fun writeSupport(
        model: SupportModel,
        callback: (String, Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback("User not logged in", false)
            return
        }

        val supportId = supportRef.push().key
        if (supportId == null) {
            callback("Failed to generate support ID", false)
            return
        }

        val updatedSupport = model.copy(uid = uid)

        supportRef.child(supportId)
            .setValue(updatedSupport)
            .addOnSuccessListener {
                callback("Support request submitted", true)
            }
            .addOnFailureListener {
                callback(it.message ?: "Failed to submit support", false)
            }
    }

    override suspend fun fetchSupportMessages(
        callback: (List<SupportModel>) -> Unit
    ) {
        supportRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(SupportModel::class.java)
                }
                callback(list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    override suspend fun replyToSupport(
        supportId: String,
        replyMessage: String,
        callback: (String, Boolean) -> Unit
    ) {
        supportRef.child(supportId)
            .child("reply")
            .setValue(replyMessage)
            .addOnSuccessListener {
                callback("Reply sent successfully", true)
            }
            .addOnFailureListener {
                callback(it.message ?: "Failed to send reply", false)
            }
    }
}
