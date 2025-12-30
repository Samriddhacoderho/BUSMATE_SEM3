package com.example.busmate.data

import android.util.Log
import com.example.busmate.model.ChildModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Add to AttendanceRepository interface first, then implement:
    override fun submitAttendance(
        busId: String,
        attendanceList: List<Map<String, Any?>>,
        callback: (Boolean) -> Unit
    ) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
            .child(date)
            .child(busId)

        val updates = mutableMapOf<String, Any>()
        attendanceList.forEach { record ->
            val studentId = record["studentId"] as? String ?: return@forEach
            updates[studentId] = record
        }

        attendanceRef.updateChildren(updates).addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }

    // Add to AttendanceRepository.kt
    override fun getAttendanceHistory(date: String, busId: String, callback: (List<Map<String, Any?>>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("attendance")
            .child(date)
            .child(busId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<Map<String, Any?>>()
                for (childSnap in snapshot.children) {
                    val data = childSnap.value as? Map<String, Any?>
                    if (data != null) historyList.add(data)
                }
                callback(historyList)
            }
            override fun onCancelled(error: DatabaseError) = callback(emptyList())
        })
    }

    // Inside AttendanceRepositoryImpl.kt
    override fun getAttendanceForParent(parentUid: String, date: String, callback: (List<Map<String, Any?>>) -> Unit) {
        val result = mutableListOf<Map<String, Any?>>()
        val parentRef = FirebaseDatabase.getInstance().getReference("users").child(parentUid).child("children")

        parentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(emptyList()) // Return early if no children found
                    return
                }

                val children = snapshot.children.mapNotNull { it.getValue(ChildModel::class.java) }
                // Group children by busId to minimize Firebase calls
                val childrenByBus = children.groupBy { it.busRouteId }
                var pendingRequests = childrenByBus.size

                if (pendingRequests == 0) {
                    callback(emptyList())
                    return
                }

                childrenByBus.forEach { (busId, childList) ->
                    if (busId.isNullOrEmpty()) {
                        pendingRequests--
                        if (pendingRequests == 0) callback(result)
                        return@forEach
                    }

                    FirebaseDatabase.getInstance().getReference("attendance")
                        .child(date).child(busId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(attSnap: DataSnapshot) {
                                childList.forEach { child ->
                                    // FIX: Check if the child exists in the attendance record
                                    val record = attSnap.child(child.studentId)
                                    val status = record.child("status").getValue(String::class.java) ?: "Absent"

                                    result.add(mapOf(
                                        "studentId" to child.studentId,
                                        "childName" to "${child.firstName} ${child.lastName}",
                                        "busRouteId" to busId,
                                        "status" to status
                                    ))
                                }
                                pendingRequests--
                                if (pendingRequests == 0) callback(result)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                pendingRequests--
                                if (pendingRequests == 0) callback(result)
                            }
                        })
                }
            }
            override fun onCancelled(p0: DatabaseError) { callback(emptyList()) }
        })
    }
}