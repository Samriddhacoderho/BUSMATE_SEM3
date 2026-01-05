package com.example.busmate.viewmodel

import android.content.Context
import android.net.Uri
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

    fun clearMessage() {
        _message.value = ""
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        schoolId: String,
        phone: String,
        password: String,

        ) {
        val user = UserModel(
            firstName = firstName,
            lastName = lastName,
            email = email,
            schoolId = schoolId,
            phone = phone
        )
        _message.value = "Loading..."

        repository.registerUser(user, password) { success, msg, data ->
            _message.value = msg
            _user.value = data
        }
    }

    fun login(userId: String, password: String) {

        _message.value = "Loading..."

        repository.loginUser(userId, password) { success, msg, user ->
            _message.value = msg
            if (success) {
                _user.value = user
            }
        }
    }


    //    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {
//        viewModelScope.launch {
//            _message.value = "Loading"
//
//            // Input Validation (Pass/Confirm match, not blank, length)
//            if (newPass.isBlank() || confirmPass.isBlank() || oldPass.isBlank()) {
//                _message.value = "All password fields must be filled."
//                return@launch
//            }
//            if (newPass != confirmPass) {
//                _message.value = "New password and confirmation password do not match."
//                return@launch
//            }
//            if (newPass.length < 6) {
//                _message.value = "New password is too short (minimum 6 characters)."
//                return@launch
//            }
//
//            try {
//                // Call the Repository
//                val result = repository.changePassword(oldPass, newPass)
//
//                // Update state
//                _message.value =
//                    if (result.isSuccess) "Password successfully changed!"
//                    else result.exceptionOrNull()?.message ?: "Password change failed."
//
//            } catch (e: Exception) {
//                _message.value = e.toString()
//            }
//        }
//    }
    fun changePassword(oldPass: String, newPass: String, confirmPass: String) {

        _message.value = ""


        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _message.value = "All password fields must be filled."
            return
        }

        if (newPass != confirmPass) {
            _message.value = "New Password and Confirm Password don't match."
            return
        }

        if (newPass.length < 8) {
            _message.value = "Password must be at least 8 characters."
            return
        }
        if (newPass == oldPass) {
            _message.value = "New password cannot be the same as the old password."
            return
        }

        _message.value = "Loading..."

        repository.changePassword(oldPass, newPass) { success, msg ->
            _message.value = msg
        }
    }



    fun resetPassword(email: String) {
        _message.value = "Loading..."

        repository.sendPasswordResetEmail(email) { msg, _ ->
            _message.value = msg
        }
    }


//

    fun loadUserProfile(userId: String) {
        _message.value = "Loading Profile..."

        repository.getUserProfile(userId) { success, msg, user ->
            _message.value = msg
            if (success) _user.value = user
        }
    }



    fun updateUserProfile(firstName: String, lastName: String, phone: String) {
        val currentUser = _user.value ?: return

        _message.value = "Updating Profile..."

        repository.updateUserProfile(currentUser.uid, firstName, lastName, phone) { success, msg ->
            _message.value = msg
            if (success) {
                _user.value = currentUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone
                )
            }
        }


    }
    fun uploadProfileImage(context: Context, uri: Uri) {
        _message.value = "Uploading image..."
        repository.uploadImage(context, uri) { imageUrl ->
            if (imageUrl != null) {
                val uid = _user.value?.uid ?: ""
                // Update the profileImage field in Firebase
                repository.updateUserField(uid, "profileImage", imageUrl) { success, msg ->
                    if (success) {
                        _user.value = _user.value?.copy(profileImage = imageUrl)
                        _message.value = "Profile image updated!"
                    } else {
                        _message.value = msg
                    }
                }
            } else {
                _message.value = "Upload failed."
            }
        }
    }
//    fun uploadImage(context: Context,imageUri: Uri,callback:(String?)-> Unit) {
//        repository.uploadImage(context,imageUri,callback)
//    }
}
//testing profile screen





