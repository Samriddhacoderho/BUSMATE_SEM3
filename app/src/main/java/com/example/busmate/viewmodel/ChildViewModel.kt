package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.ChildRepositoryInterface
import com.example.busmate.model.ChildModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Ensure the repository is injected (via DI framework or a simple factory)
class ChildViewModel(private val repository: ChildRepositoryInterface) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    /**
     * Adds a new child. Accepts individual strings from the UI layer.
     */
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
            _message.value = "Loading"

            // Basic Input Validation
            if (firstName.isBlank() || studentId.isBlank() || busRouteId.isBlank()) {
                _message.value = "First Name, Student ID, and Bus Route ID are required."
                return@launch
            }

            try {
                // 1. Create the ChildModel
                val newChild = ChildModel(
                    firstName = firstName,
                    lastName = lastName,
                    studentId = studentId,
                    busRouteId = busRouteId,
                    pickUpLocation = pickUpLocation,
                    dropOffLocation = dropOffLocation
                )

                // 2. Call the repository function
                repository.addChild(model = newChild) { responseMessage, success ->
                    _message.value = responseMessage
                    _isSuccess.value = success
                }

            } catch (e: Exception) {
                _message.value = "An unexpected error occurred: ${e.message}"
                _isSuccess.value = false
            }
        }
    }

    /**
     * Clears the message state, preventing the Toast from showing again on rotation or
     * after a success/failure message has been displayed. (Fix for Activity code)
     */
    fun clearMessage() {
        _message.value = ""
    }
}