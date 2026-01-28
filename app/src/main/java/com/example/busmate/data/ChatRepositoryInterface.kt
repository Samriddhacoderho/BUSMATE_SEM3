package com.example.busmate.data

import com.example.busmate.model.ChildModel

interface ChatRepositoryInterface {
    suspend fun generateResponse(userQuestion: String, children: List<ChildModel>): String
}