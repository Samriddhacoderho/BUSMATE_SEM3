package com.example.busmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.busmate.data.AttendanceRepository
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.ChildModel
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

class AttendanceViewModel(
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl(),
    private val attendanceRepo: AttendanceRepository = AttendanceRepositoryImpl()
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildModel>>(emptyList())
    val children: StateFlow<List<ChildModel>> = _children

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Helper to store bus ID after loading the list
    private var currentBusId: String? = null

    fun loadAttendanceList(driverUid: String) {
        _isLoading.value = true
        busRepo.getBusByDriverUid(driverUid) { bus ->
            if (bus != null) {
                currentBusId = bus.uid
                attendanceRepo.getChildrenByRouteId(bus.routeId) { list ->
                    _children.value = list
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    // 🔹 FIXED SIGNATURE: 3 parameters to match the Activity call
    fun submitAttendance(
        driverUid: String,
        selectedChildren: List<ChildModel>,
        onResult: (Boolean) -> Unit
    ) {
        // If we don't have the busId yet, fetch it first
        if (currentBusId == null) {
            busRepo.getBusByDriverUid(driverUid) { bus ->
                if (bus != null) {
                    currentBusId = bus.uid
                    performSubmit(bus.uid, selectedChildren, onResult)
                } else {
                    onResult(false)
                }
            }
        } else {
            performSubmit(currentBusId!!, selectedChildren, onResult)
        }
    }

    private fun performSubmit(busId: String, selectedChildren: List<ChildModel>, onResult: (Boolean) -> Unit) {
        // Get the full list of students we loaded for this route
        val allChildren = _children.value

        val attendanceData = allChildren.map { child ->
            // Check if this specific child was in the checked list
            val isPresent = selectedChildren.any { it.studentId == child.studentId }

            mapOf(
                "studentId" to child.studentId,
                "childName" to "${child.firstName} ${child.lastName}",
                "status" to if (isPresent) "Present" else "Absent", // 🔹 Dynamic status
                "timestamp" to com.google.firebase.database.ServerValue.TIMESTAMP
            )
        }

        attendanceRepo.submitAttendance(busId, attendanceData, onResult)
    }
}