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

}
