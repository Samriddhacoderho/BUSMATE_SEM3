package com.example.busmate.data
import com.example.busmate.model.EmergencyModel
import com.google.firebase.database.*

class EmergencyRepositoryImpl : EmergencyRepositoryInterface {
    private val db = FirebaseDatabase.getInstance().getReference("emergency_alerts")

    override fun sendSOS(alert: EmergencyModel, onComplete: (Boolean) -> Unit) {
        val key = db.push().key ?: return
        db.child(key).setValue(alert.copy(id = key)).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    override fun observeAlerts(callback: (List<EmergencyModel>) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children.mapNotNull { it.getValue(EmergencyModel::class.java) }
                callback(alerts.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}