package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.ChildModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttendanceRepositoryImpl: AttendanceRepository{
    // Add this to your UserRepositoryInterface and implementation
    // Add this to UserRepositoryImpl.kt
    override fun getChildrenByRouteId(routeId: String, callback: (List<ChildModel>) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childrenList = mutableListOf<ChildModel>()

                for (userSnapshot in snapshot.children) {
                    val childrenSnapshot = userSnapshot.child("children")
                    if (childrenSnapshot.exists()) {
                        for (childSnap in childrenSnapshot.children) {
                            val child = childSnap.getValue(ChildModel::class.java)
                            // Use trim() because some names in your screenshot have trailing spaces
                            if (child?.busRouteId?.trim() == routeId.trim()) {
                                childrenList.add(child)
                            }
                        }
                    }
                }
                callback(childrenList)
            }
            override fun onCancelled(error: DatabaseError) { callback(emptyList()) }
        })
    }
}