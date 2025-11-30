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

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        schoolId: String,
        phone: String,
        password: String
    ) {
        viewModelScope.launch {

            _message.value = "Loading"

            try {
                val user = UserModel(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    schoolId = schoolId,
                    phone = phone
                )

                val result = repository.registerUser(user, password)

                _message.value =
                    if (result.isSuccess) "Successful Registration"
                    else "Registration Unsuccessful"

            } catch (e: Exception) {
                _message.value = e.toString()
            }
        }
    }
}
