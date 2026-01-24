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

    fun adminPreAddStudentId(studentId: String, callback: (String, Boolean) -> Unit)
    fun adminAddChildToParent(
        parentSchoolId: String, // The ID the admin gave the parent
        child: ChildModel,
        callback: (String, Boolean) -> Unit
    )
    fun uploadChildImage(
        context: android.content.Context,
        imageUri: android.net.Uri,
        callback: (String?) -> Unit
    )
    fun verifyParentExists(parentSchoolId: String, callback: (Boolean) -> Unit)

}
//testing getAllAvailableRoutes





































