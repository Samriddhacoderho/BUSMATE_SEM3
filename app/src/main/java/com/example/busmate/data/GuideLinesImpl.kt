package com.example.busmate.data

import com.example.busmate.model.GuidelineModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GuideLinesImpl : GuideLinesInterface{
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val guidelinesRef = db.getReference("guidelines")

    override fun updateGuidelines(content: String, callback: (Boolean, String) -> Unit) {
        guidelinesRef.setValue(GuidelineModel(content))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, "Guidelines updated!")
                else callback(false, task.exception?.message ?: "Update failed")
            }
    }

    override fun getGuidelines(callback: (Boolean, String, String?) -> Unit) {
        guidelinesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(GuidelineModel::class.java)
                callback(true, "Loaded", data?.content)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}