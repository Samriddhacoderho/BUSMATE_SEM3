package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.SupportRepositoryInterface
import com.example.busmate.model.SupportModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupportViewModel(val repository: SupportRepositoryInterface) : ViewModel(){
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun writeReport(name: String,typeofUser:String,title:String,mess_age:String){
        viewModelScope.launch {
            _message.value="Loading"
            try {
                val support= SupportModel(
                    name=name,
                    typeofUser=typeofUser,
                    title=title,
                    message = mess_age
                )

                repository.writeSupport(support) { responseMessage, success ->
                    _message.value = responseMessage
                }

            }catch (e: Exception){
                _message.value = e.toString()
            }
        }
    }
}
