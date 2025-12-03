package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateAccountViewModel(private val repository: UserRepositoryInterface) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun createAccountWithMinimalData(role: String, schoolId: String) {

        if (role == "Select Role") {
            _message.value = "Please select a role"
            return
        }

        if (schoolId.isBlank()) {
            _message.value = "User ID cannot be empty"
            return
        }

        viewModelScope.launch {
            _loading.value = true

            val user = UserModel(
                role = role,
                schoolId = schoolId,
                email = "$schoolId@busmate.com", // dummy auto-email required by Firebase
                firstName = "",
                lastName = "",
                phone = ""
            )

            val password = "123456" // default password

            val result = repository.registerUser(user, password)

            _message.value = if (result.isSuccess) {
                "Account Created Successfully"
            } else {
                result.exceptionOrNull()?.message ?: "Failed"
            }

            _loading.value = false
        }
    }
}
