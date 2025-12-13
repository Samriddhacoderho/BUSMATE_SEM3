package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.UserRepositoryImpl
import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepositoryInterface) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        schoolId: String,
        phone: String,
        password: String,

        ) {
        viewModelScope.launch {

            _message.value = "Loading"

            try {
                val user = UserModel(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    schoolId = schoolId,
                    phone = phone,


                    )

                val result = repository.registerUser(user, password)

                _message.value =
                    if (result.isSuccess) "Successful Registration"
                    else result.exceptionOrNull()?.message ?: "Unknown Error"

            } catch (e: Exception) {
                _message.value = e.toString()
            }
        }
    }

    fun login(userID: String, password: String) {
        viewModelScope.launch {
            _message.value = "Loading"
            try {
                val result = repository.loginUser(userID, password)
                if (result.isSuccess) {
                    _message.value = "Successful Login"
                    _user.value = result.getOrNull() // This updates the user state flow
                } else {
                    _message.value = result.exceptionOrNull()?.message ?: "Unknown Error"
                }
            } catch (e: Exception) {
                _message.value = e.toString()
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
        viewModelScope.launch {
            _message.value = "Loading"

            // Input Validation (Pass/Confirm match, not blank, length)
            if (newPass.isBlank() || confirmPass.isBlank() || oldPass.isBlank()) {
                _message.value = "All password fields must be filled."
                return@launch
            }
            if (newPass != confirmPass) {
                _message.value = "New password and confirmation password do not match."
                return@launch
            }
            if (newPass.length < 6) {
                _message.value = "New password is too short (minimum 6 characters)."
                return@launch
            }

            try {
                // Call the Repository
                val result = repository.changePassword(oldPass, newPass)

                // Update state
                _message.value =
                    if (result.isSuccess) "Password successfully changed!"
                    else result.exceptionOrNull()?.message ?: "Password change failed."

            } catch (e: Exception) {
                _message.value = e.toString()
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _message.value = "Loading"
            // The repository is now UserRepositoryInterface, which implements the function
            repository.sendPasswordResetEmail(email) { msg, success ->
                _message.value = msg
            }
        }
    }
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _message.value = "Loading Profile..."
            try {
                val result = repository.getUserProfile(userId) // Method to load user profile data
                _user.value = result.getOrNull()
            } catch (e: Exception) {
                _message.value = "Failed to load user profile: ${e.message}"
            }
        }
    }
    fun updateUserProfile(firstName: String, lastName: String, phone: String) {
        viewModelScope.launch {
            _message.value = "Updating Profile..."

            // Get the current user (you might need to get this from the auth session)
            val currentUser = _user.value
            if (currentUser != null) {
                val result = repository.updateUserProfile(currentUser.uid, firstName, lastName, phone)

                _message.value = if (result.isSuccess) {
                    "Profile Updated Successfully!"
                } else {
                    result.exceptionOrNull()?.message ?: "Error Updating Profile"
                }
            } else {
                _message.value = "User not found!"
            }
        }
    }
}




