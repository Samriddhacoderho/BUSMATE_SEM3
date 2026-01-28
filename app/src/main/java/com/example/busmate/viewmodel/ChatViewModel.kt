package com.example.busmate.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.ChatRepositoryImpl
import com.example.busmate.data.ChatRepositoryInterface
import com.example.busmate.model.ChatMessageModel
import com.example.busmate.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Changed to AndroidViewModel to access Application Context
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // Pass context to repository
    private val repository = ChatRepositoryImpl(application.applicationContext)

    private val _messages = MutableStateFlow<List<ChatMessageModel>>(
        listOf(ChatMessageModel("Hello! Ask me about your child's attendance or the bus location.", false))
    )
    val messages: StateFlow<List<ChatMessageModel>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(question: String, user: UserModel?) {
        if (question.isBlank() || user == null) return

        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessageModel(question, true))
        _messages.value = currentList
        _isLoading.value = true

        viewModelScope.launch {
            val children = user.children.values.toList()
            val responseText = repository.generateResponse(question, children)

            val updatedList = _messages.value.toMutableList()
            updatedList.add(ChatMessageModel(responseText, false))
            _messages.value = updatedList
            _isLoading.value = false
        }
    }
}