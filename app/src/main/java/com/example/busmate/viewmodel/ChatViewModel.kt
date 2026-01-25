package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busmate.data.ChatRepository
import com.example.busmate.data.ChatMessage
import com.example.busmate.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! Ask me about your child's attendance or bus status.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(question: String, user: UserModel?) {
        if (question.isBlank() || user == null) return

        // 1. Show User Message
        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage(question, true))
        _messages.value = currentList
        _isLoading.value = true

        viewModelScope.launch {
            // 2. Get Children from User Model
            val children = user.children.values.toList()

            // 3. Call Repo
            val responseText = repository.generateResponse(question, children)

            // 4. Show AI Response
            val updatedList = _messages.value.toMutableList()
            updatedList.add(ChatMessage(responseText, false))
            _messages.value = updatedList
            _isLoading.value = false
        }
    }
}