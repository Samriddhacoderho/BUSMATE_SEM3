package com.example.busmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.busmate.data.AttendanceRepositoryImpl
import com.example.busmate.data.BusRepositoryImpl
import com.example.busmate.data.BusRepositoryInterface
import com.example.busmate.model.ChildModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

class AttendanceViewModel(
    private val busRepo: BusRepositoryInterface = BusRepositoryImpl(),
    private val attendanceRepo: AttendanceRepositoryImpl = AttendanceRepositoryImpl()
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildModel>>(emptyList())
    val children: StateFlow<List<ChildModel>> = _children

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAttendanceList(driverUid: String) {
        _isLoading.value = true
        Log.d("AttendanceFlow", "Step 1: Fetching bus for Driver UID: $driverUid")

        busRepo.getBusByDriverUid(driverUid) { bus ->
            if (bus != null) {
                Log.d("AttendanceFlow", "Step 2: Found Bus! Route ID is: ${bus.routeId}")

                attendanceRepo.getChildrenByRouteId(bus.routeId) { list ->
                    Log.d("AttendanceFlow", "Step 4: Found ${list.size} children for route ${bus.routeId}")
                    _children.value = list
                    _isLoading.value = false
                }
            } else {
                Log.e("AttendanceFlow", "Step 2 Error: No bus found linked to this Driver UID.")
                _isLoading.value = false
            }
        }
    }
}