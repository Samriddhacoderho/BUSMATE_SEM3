package com.example.busmate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.busmate.data.GuideLinesInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GuideLineViewModel(private val repository: GuideLinesInterface) : ViewModel(){
    private val _guidelines = MutableStateFlow("")
    val guidelines: StateFlow<String> = _guidelines
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun loadGuidelines() {
        _message.value = "Loading..."
        repository.getGuidelines { success, msg, content ->
            if (success) _guidelines.value = content ?: ""
            _message.value = msg
        }
    }

    fun postGuidelines(content: String) {
        _message.value = "Posting..."
        repository.updateGuidelines(content) { success, msg ->
            _message.value = msg
            if (success) _guidelines.value = content
        }
    }
}