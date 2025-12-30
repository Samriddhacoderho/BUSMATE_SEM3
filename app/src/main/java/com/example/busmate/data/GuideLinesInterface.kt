package com.example.busmate.data

interface GuideLinesInterface {
    fun updateGuidelines(content: String, callback: (Boolean, String) -> Unit)
    fun getGuidelines(callback: (Boolean, String, String?) -> Unit)
}