package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.CreateAccountModel
import com.example.busmate.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateAccountViewModel(private val repository: UserRepositoryInterface) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message



    fun createAccountWithMinimalData(role: String, schoolId: String) {

        viewModelScope.launch {
            _message.value = "Loading"
            try{
                val model= CreateAccountModel(
                    role = role,
                    schoolId = schoolId
                )
                repository.createAccount(model=model) { responseMessage, success ->
                    _message.value = responseMessage
                }


            }catch (e: Exception){
                _message.value = e.toString()
            }

        }
    }
}
