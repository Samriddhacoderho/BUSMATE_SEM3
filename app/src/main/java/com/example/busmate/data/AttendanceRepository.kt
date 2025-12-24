package com.example.busmate.data

import com.example.busmate.model.ChildModel

interface AttendanceRepository {
    fun getChildrenByRouteId(routeId:String, callback:(List<ChildModel>)->Unit)
}