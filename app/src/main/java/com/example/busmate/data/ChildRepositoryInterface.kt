package com.example.busmate.data

import com.example.busmate.model.ChildModel

interface ChildRepositoryInterface {

    fun addChild(
        model: ChildModel,
        callback: (String, Boolean) -> Unit
    )
    fun observeChildren(
        parentUid: String,
        callback: (List<ChildModel>) -> Unit
    )
    fun observeAllChildren(callback: (List<ChildModel>) -> Unit)
    fun updateChild(model: ChildModel, callback: (String, Boolean) -> Unit)
    fun getAllAvailableRoutes(callback: (List<String>) -> Unit)

}
//testing getAllAvailableRoutes
