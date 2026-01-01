package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.AttendanceRepository
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.BusModel
import com.example.busmate.model.ChildModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AttendanceViewModel(
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl(),
    private val attendanceRepo: AttendanceRepository = AttendanceRepositoryImpl()
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildModel>>(emptyList())
    val children: StateFlow<List<ChildModel>> = _children

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allBuses = MutableStateFlow<List<BusModel>>(emptyList())
    val allBuses: StateFlow<List<BusModel>> = _allBuses

    private val _attendanceHistory = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val attendanceHistory: StateFlow<List<Map<String, Any?>>> = _attendanceHistory

    private val _parentAttendance = MutableStateFlow<List<Map<String, Any?>>>(emptyList())
    val parentAttendance: StateFlow<List<Map<String, Any?>>> = _parentAttendance

    private val _todayStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())

    // 🔹 Changed variable name to track Route Name instead of UID
    private var currentBusRouteId: String? = null

    fun loadAttendanceList(driverUid: String) {
        _isLoading.value = true
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        busRepo.getBusByDriverUid(driverUid) { bus ->
            if (bus != null) {
                currentBusRouteId = bus.routeId

                // 1. Fetch children on this route
                attendanceRepo.getChildrenByRouteId(bus.routeId) { roster ->

                    // 2. Fetch today's existing attendance for this bus route
                    // (Note: Using bus.routeId as the key based on your previous performSubmit logic)
                    attendanceRepo.getAttendanceForDateAndBus(today, bus.routeId) { existingRecords ->

                        // Convert list to map: studentId -> status
                        val statusMap = existingRecords.associate {
                            (it["studentId"] as String) to (it["status"] as String)
                        }

                        _todayStatusMap.value = statusMap
                        _children.value = roster
                        _isLoading.value = false
                    }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Helper function for the Activity to check initial status
    fun getInitialStatus(studentId: String): Boolean {
        return _todayStatusMap.value[studentId] == "Present"
    }

    fun submitAttendance(
        driverUid: String,
        selectedChildren: List<ChildModel>,
        onResult: (Boolean) -> Unit
    ) {
        if (currentBusRouteId == null) {
            busRepo.getBusByDriverUid(driverUid) { bus ->
                if (bus != null) {
                    currentBusRouteId = bus.routeId
                    performSubmit(bus.routeId, selectedChildren, onResult)
                } else {
                    onResult(false)
                }
            }
        } else {
            performSubmit(currentBusRouteId!!, selectedChildren, onResult)
        }
    }

    // 🔹 For Admin: identifier is now the routeId passed from Activity
    fun loadHistory(date: String, routeId: String) {
        _isLoading.value = true
        attendanceRepo.getAttendanceHistory(date, routeId) { list ->
            _attendanceHistory.value = list
            _isLoading.value = false
        }
    }

    fun loadParentAttendance(parentUid: String, date: String) {
        _isLoading.value = true
        attendanceRepo.getAttendanceForParent(parentUid, date) { list ->
            _parentAttendance.value = list
            _isLoading.value = false
        }
    }

    private fun performSubmit(routeId: String, selectedChildren: List<ChildModel>, onResult: (Boolean) -> Unit) {
        val allChildren = _children.value

        val attendanceData = allChildren.map { child ->
            val isPresent = selectedChildren.any { it.studentId == child.studentId }
            mapOf(
                "studentId" to child.studentId,
                "childName" to "${child.firstName} ${child.lastName}",
                "status" to if (isPresent) "Present" else "Absent",
                "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
            )
        }
        // Submits to attendance/date/RouteName
        attendanceRepo.submitAttendance(routeId, attendanceData, onResult)
    }

    fun fetchAllBusesForAdmin() {
        busRepo.getAllBusesLive { list ->
            _allBuses.value = list.filterNotNull()
        }
    }
}