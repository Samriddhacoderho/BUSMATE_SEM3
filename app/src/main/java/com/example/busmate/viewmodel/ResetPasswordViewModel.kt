package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.ResetPasswordRepositoryImpl
import com.example.busmate.data.ResetPasswordRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {

    private val repository = ResetPasswordRepositoryImpl()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _message.value = "Loading"

            repository.sendPasswordResetEmail(email) { msg, success ->
                _message.value = msg
            }
        }
    }
}
