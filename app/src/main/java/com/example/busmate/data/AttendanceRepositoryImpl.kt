package com.example.busmate.data

import com.example.busmate.model.ChildModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
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

//     Add to AttendanceRepository interface first, then implement:
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
            sendAttendanceNotifications(busId)
            callback(task.isSuccessful)
        }
    }

    private fun sendAttendanceNotifications(routeId: String) {
        val database = FirebaseDatabase.getInstance()

        // 1. Get the Bus Number/Details first to make the message pretty
        database.getReference("buses").orderByChild("busNumber").equalTo(routeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Note: routeId in your JSON matches 'busNumber' in the buses node
                    val busMsg = "Bus $routeId has updated today's attendance."

                    // 2. Notify Admin
                    val adminNotification = mapOf(
                        "title" to "Attendance Update",
                        "message" to busMsg,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "type" to "attendance_update"
                    )
                    database.getReference("notifications").child("admin").push().setValue(adminNotification)

                    // 3. Notify Relevant Parents
                    database.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            for (userSnap in userSnapshot.children) {
                                val childrenSnap = userSnap.child("children")
                                var isLinkedToBus = false

                                if (childrenSnap.exists()) {
                                    for (child in childrenSnap.children) {
                                        val childBusId = child.child("busRouteId").getValue(String::class.java)
                                        if (childBusId?.trim() == routeId.trim()) {
                                            isLinkedToBus = true
                                            break
                                        }
                                    }
                                }

                                if (isLinkedToBus) {
                                    val parentUid = userSnap.key
                                    if (parentUid != null) {
                                        val parentNotification = mapOf(
                                            "title" to "Attendance Alert",
                                            "message" to "Attendance for your child's bus ($routeId) has been marked.",
                                            "timestamp" to ServerValue.TIMESTAMP,
                                            "type" to "attendance_update"
                                        )
                                        database.getReference("notifications").child(parentUid).push().setValue(parentNotification)
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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

    // Replace the getAttendanceForParent method in AttendanceRepositoryImpl.kt

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
                                    // Check if the child exists in the attendance record
                                    val record = attSnap.child(child.studentId)

                                    // If record doesn't exist, show "No Record"
                                    // If record exists, use the status value
                                    val status = if (record.exists()) {
                                        record.child("status").getValue(String::class.java) ?: "No Record"
                                    } else {
                                        "No Record"  // Changed from "Absent"
                                    }

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
    // In AttendanceRepositoryImpl.kt

    override fun getAttendanceForDateAndBus(date: String, busId: String, callback: (List<Map<String, Any?>>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("attendance")
            .child(date)
            .child(busId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, Any?>>()
                for (childSnap in snapshot.children) {
                    val data = childSnap.value as? Map<String, Any?>
                    if (data != null) {
                        list.add(data)
                    }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}