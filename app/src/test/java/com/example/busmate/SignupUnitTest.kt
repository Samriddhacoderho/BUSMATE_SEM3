package com.example.busmate

import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.argThat


class SignupUnitTest {

    @Test
    fun register_success_test() {
        // Arrange - Create mock repository and ViewModel
        val repo = mock<UserRepositoryInterface>()
        val viewModel = UserViewModel(repo)

        // Mock the registerUser method to return success
        // The signature is: registerUser(user: UserModel, password: String, callback: (Boolean, String, UserModel?) -> Unit)
        doAnswer { invocation ->
            // Get the callback (3rd parameter)
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(2)

            // Create a test user to return
            val testUser = UserModel(
                uid = "testUserId123",
                firstName = "Ram",
                lastName = "Thapa",
                email = "ram@gmail.com",
                schoolId = "ATX6647",
                phone = "9876543210"
            )

            // Call the callback with success
            callback(true, "Successful Registration", testUser)
            null
        }.`when`(repo).registerUser(any(), any(), any())

        // Act - Call the register method
        viewModel.register(
            firstName = "Ram",
            lastName = "Thapa",
            email = "ram@gmail.com",
            schoolId = "ATX6647",
            phone = "9876543210",
            password = "Password@123"
        )

        // Wait a bit for the callback to execute
        Thread.sleep(100)

        // Assert - Verify results
        assertEquals("Successful Registration", viewModel.message.value)

        // Verify the repo method was called with correct UserModel
        verify(repo).registerUser(
            argThat { user ->
                user.firstName == "Ram" &&
                        user.lastName == "Thapa" &&
                        user.email == "ram@gmail.com" &&
                        user.schoolId == "ATX6647" &&
                        user.phone == "9876543210"
            },
            argThat { password -> password == "Password@123" },
            any()
        )
    }

    @Test
    fun register_invalidPhone_test() {
        // Arrange
        val repo = mock<UserRepositoryInterface>()
        val viewModel = UserViewModel(repo)

        // Act - Try to register with invalid phone number
        viewModel.register(
            firstName = "Ram",
            lastName = "Thapa",
            email = "ram@gmail.com",
            schoolId = "ATX6647",
            phone = "123", // Invalid phone
            password = "Password@123"
        )

        Thread.sleep(100)

        // Assert - Should show error message
        val message = viewModel.message.value
        assertEquals("Enter valid Nepali phone number", message)
    }

    @Test
    fun register_validNepaliPhone_test() {
        // Arrange
        val repo = mock<UserRepositoryInterface>()
        val viewModel = UserViewModel(repo)

        // Mock successful registration
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(2)
            val testUser = UserModel(
                uid = "testUserId123",
                firstName = "Ram",
                lastName = "Thapa",
                email = "ram@gmail.com",
                schoolId = "12345",
                phone = "9876543210"
            )
            callback(true, "Successful Registration", testUser)
            null
        }.`when`(repo).registerUser(any(), any(), any())

        // Act - Register with valid Nepali phone
        viewModel.register(
            firstName = "Ram",
            lastName = "Thapa",
            email = "ram@gmail.com",
            schoolId = "12345",
            phone = "9876543210", // Valid Nepali phone
            password = "Password@123"
        )

        Thread.sleep(100)

        // Assert
        assertEquals("Successful Registration", viewModel.message.value)
    }
}