package com.example.busmate.data

import com.example.busmate.model.ChildModel

interface AttendanceRepository {
    fun getChildrenByRouteId(routeId: String, callback: (List<ChildModel>) -> Unit)

    // Add this function to the interface
    fun submitAttendance(
        busId: String,
        attendanceList: List<Map<String, Any?>>,
        callback: (Boolean) -> Unit
    )

    fun getAttendanceHistory(
        date: String,
        busId: String,
        callback: (List<Map<String, Any?>>) -> Unit
    )

    fun getAttendanceForParent(
        parentUid: String,
        date: String,
        callback: (List<Map<String, Any?>>) -> Unit
    )

}