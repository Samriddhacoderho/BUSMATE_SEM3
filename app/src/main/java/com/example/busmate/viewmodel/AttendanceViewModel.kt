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

}