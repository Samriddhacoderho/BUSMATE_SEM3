package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.SupportRepositoryInterface
import com.example.busmate.model.SupportModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SupportViewModel(
    private val repository: SupportRepositoryInterface
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _supportMessages = MutableStateFlow<List<SupportModel>>(emptyList())
    val supportMessages: StateFlow<List<SupportModel>> = _supportMessages

    fun writeReport(
        name: String,
        typeofUser: String?,
        title: String,
        mess_age: String
    ) {
        _message.value = "Loading..."

        val support = SupportModel(
            name = name,
            typeofUser = typeofUser,
            title = title,
            message = mess_age
        )

        repository.writeSupport(support) { success, msg ->
            _message.value = msg
        }
    }

    fun fetchSupportMessages() {
        _message.value = "Loading..."

        repository.fetchSupportMessages { success, msg, list ->
            _message.value = msg
            if (success) {
                _supportMessages.value = list
            }
        }
    }

    fun replyToSupport(supportId: String, replyMessage: String) {
        _message.value = "Sending reply..."

        repository.replyToSupport(supportId, replyMessage) { success, msg ->
            _message.value = msg
            if (success) {
                fetchSupportMessages()
            }
        }
    }
}
