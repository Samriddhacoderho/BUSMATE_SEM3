package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.ChildRepositoryInterface
import com.example.busmate.model.ChildModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChildViewModel(
    private val repository: ChildRepositoryInterface
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    // ✅ EXPOSE children as Compose State
    private val _children = MutableStateFlow<List<ChildModel>>(emptyList())
    val children: StateFlow<List<ChildModel>> = _children

    fun addChild(
        firstName: String,
        lastName: String,
        studentId: String,
        busRouteId: String,
        pickUpLocation: String,
        dropOffLocation: String,
        pLat: Double,
        pLng: Double,
        dLat: Double,
        dLng: Double
    ) {
        _message.value = ""
        _isSuccess.value = false

        val child = ChildModel(
            firstName = firstName,
            lastName = lastName,
            studentId = studentId,
            busRouteId = busRouteId,
            pickUpLocation = pickUpLocation,
            dropOffLocation = dropOffLocation,
            pickUpLat = pLat,
            pickUpLng = pLng,
            dropOffLat = dLat,
            dropOffLng = dLng
        )

        repository.addChild(child) { responseMessage, success ->
            _message.value = responseMessage
            _isSuccess.value = success
        }
    }

    fun observeChildren(parentUid: String) {
        repository.observeChildren(parentUid) { list ->
            _children.value = list   // 🔥 THIS triggers recomposition
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
    // ChildViewModel.kt
    fun observeAllChildren() {
        repository.observeAllChildren { list ->
            _children.value = list // Updates the StateFlow for the UI
        }
    }
    fun updateChild(child: ChildModel) {
        _message.value = "Saving changes..."
        _isSuccess.value = false
        repository.updateChild(child) { response, success ->
            _message.value = response
            _isSuccess.value = success
        }
    }
    fun resetStatus() {
        _isSuccess.value = false
        _message.value = ""
    }
}
