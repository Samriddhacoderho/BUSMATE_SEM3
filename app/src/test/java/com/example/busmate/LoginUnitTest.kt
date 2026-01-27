package com.example.busmate

import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LoginUnitTest {

    @Test
    fun login_success_test() {
        val repo = mock<UserRepositoryInterface>()
        val viewModel = UserViewModel(repo)

        // Scripting the fake response for your 3-parameter callback
        doAnswer { invocation ->
            // callback has (Boolean, String, UserModel?)
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(2)
            callback(true, "Successful Login", null)
            null
        }.`when`(repo).loginUser(eq("12345"), eq("Password@123"), any())

        viewModel.login("12345", "Password@123")

        assertEquals("Successful Login", viewModel.message.value)

        // Verify the repo was called
        verify(repo).loginUser(eq("12345"), eq("Password@123"), any())
    }
}