package com.example.busmate.data

import com.example.busmate.model.ChildModel

interface ChildRepositoryInterface {
    /**
     * Adds a new child's data to the parent's document using a nested map structure
     * and performs a global uniqueness check on the studentId using a Firestore Transaction.
     *
     * @param model The ChildModel object containing the child's data.
     * @param callback A lambda function to return the result: (message, success_status).
     */
    suspend fun addChild(model: ChildModel, callback: (String, Boolean) -> Unit)
}