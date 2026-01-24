package com.example.busmate.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.busmate.data.ChildRepositoryImpl
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
    private val _availableRoutes = MutableStateFlow<List<String>>(emptyList())
    val availableRoutes: StateFlow<List<String>> = _availableRoutes

    // UPDATED: Added context and imageUri parameters
    fun addChild(
        context: android.content.Context,
        imageUri: android.net.Uri?,
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
        _message.value = "Uploading data..."
        _isSuccess.value = false

        if (imageUri != null) {
            // Safe cast and call to the repository
            val repo = repository as? ChildRepositoryImpl
            repo?.uploadChildImage(context, imageUri) { imageUrl ->
                if (imageUrl != null) {
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
                        dropOffLng = dLng,
                        profileImage = imageUrl // This must be in your ChildModel.kt
                    )
                    repository.addChild(child) { msg, success ->
                        _message.value = msg
                        _isSuccess.value = success
                    }
                } else {
                    _message.value = "Image upload failed. Check your internet or credentials."
                    _isSuccess.value = false
                }
            }
        } else {
            // Logic for adding child without an image
            val child = ChildModel(firstName, lastName, studentId, busRouteId, pickUpLocation, dropOffLocation, pLat, pLng, dLat, dLng, "")
            repository.addChild(child) { msg, success ->
                _message.value = msg
                _isSuccess.value = success
            }
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
    // Add these to your ChildViewModel class
    fun fetchAvailableRoutes() {
        repository.getAllAvailableRoutes { routes ->
            _availableRoutes.value = routes
        }
    }

    fun preRegisterStudentId(studentId: String) {
        if (studentId.isBlank()) {
            _message.value = "Enter a valid ID"
            return
        }
        _message.value = "Processing..."
        repository.adminPreAddStudentId(studentId) { msg, success ->
            _message.value = msg
        }
    }

    // Inside ChildViewModel.kt

    fun adminAddChild(
        parentSchoolId: String,
        context: Context,
        imageUri: Uri?,
        child: ChildModel // Pass the model constructed in UI
    ) {
        _message.value = "Processing..."

        // Use your existing image upload logic here if an image is provided
        if (imageUri != null) {
            repository.uploadChildImage(context, imageUri) { imageUrl ->
                val finalChild = child.copy(profileImage = imageUrl ?: "")
                repository.adminAddChildToParent(parentSchoolId, finalChild) { msg, success ->
                    _message.value = msg
                    _isSuccess.value = success
                }
            }
        } else {
            repository.adminAddChildToParent(parentSchoolId, child) { msg, success ->
                _message.value = msg
                _isSuccess.value = success
            }
        }
    }

    // In ChildViewModel.kt
    fun validateAndPreRegister(parentSchoolId: String, studentId: String) {
        _message.value = "Processing..."

        // Step 1: Check if Parent exists
        repository.verifyParentExists(parentSchoolId) { exists ->
            if (!exists) {
                _message.value = "Error: Parent ID $parentSchoolId not found."
            } else {
                // Step 2: If Parent exists, proceed to register Student ID
                preRegisterStudentId(studentId)
            }
        }
    }
}
//testing fetchAvailableRoutes