package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.ChildRepositoryInterface
import com.example.busmate.model.ChildModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildViewModel(
    private val repository: ChildRepositoryInterface
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    fun addChild(
        firstName: String,
        lastName: String,
        studentId: String,
        busRouteId: String,
        pickUpLocation: String,
        dropOffLocation: String
    ) {
        _message.value = ""
        _isSuccess.value = false

        viewModelScope.launch {

            // Validation
            if (firstName.isBlank() || studentId.isBlank() || busRouteId.isBlank()) {
                _message.value =
                    "First Name, Student ID, and Bus Route ID are required."
                return@launch
            }

            val child = ChildModel(
                firstName = firstName,
                lastName = lastName,
                studentId = studentId,
                busRouteId = busRouteId,
                pickUpLocation = pickUpLocation,
                dropOffLocation = dropOffLocation
            )

            repository.addChild(child) { responseMessage, success ->
                _message.value = responseMessage
                _isSuccess.value = success
            }
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
}
