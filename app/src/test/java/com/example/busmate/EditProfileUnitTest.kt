package com.example.busmate

import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.UserModel
import com.example.busmate.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class EditProfileUnitTest {

    private lateinit var repo: UserRepositoryInterface
    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        repo = mock()
        viewModel = UserViewModel(repo)
    }

    @Test
    fun loadUserProfile_success_test() {
        // Arrange
        val testUser = UserModel(
            uid = "test123",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            schoolId = "240488",
            phone = "9812345678",
            profileImage = ""
        )

        // Mock the repository response
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(true, "Profile loaded successfully", testUser)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        // Act
        viewModel.loadUserProfile("test123")

        // Assert
        assertEquals("Profile loaded successfully", viewModel.message.value)
        assertNotNull(viewModel.user.value)
        assertEquals("John", viewModel.user.value?.firstName)
        assertEquals("Doe", viewModel.user.value?.lastName)
        assertEquals("9812345678", viewModel.user.value?.phone)

        // Verify the repo was called
        verify(repo).getUserProfile(eq("test123"), any())
    }

    @Test
    fun updateUserProfile_success_test() {
        // Arrange - First load a user
        val initialUser = UserModel(
            uid = "test123",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            schoolId = "240488",
            phone = "9812345678",
            profileImage = ""
        )

        // Load user first
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(true, "Profile loaded successfully", initialUser)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        viewModel.loadUserProfile("test123")

        // Mock the update response
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(4)
            callback(true, "Profile updated successfully")
            null
        }.`when`(repo).updateUserProfile(eq("test123"), eq("Jane"), eq("Smith"), eq("9823456789"), any())

        // Act
        viewModel.updateUserProfile("Jane", "Smith", "9823456789")

        // Assert
        assertEquals("Profile updated successfully", viewModel.message.value)
        assertEquals("Jane", viewModel.user.value?.firstName)
        assertEquals("Smith", viewModel.user.value?.lastName)
        assertEquals("9823456789", viewModel.user.value?.phone)

        // Verify the repo was called
        verify(repo).updateUserProfile(eq("test123"), eq("Jane"), eq("Smith"), eq("9823456789"), any())
    }

    @Test
    fun updateUserProfile_invalidPhone_test() {
        // Arrange - First load a user
        val initialUser = UserModel(
            uid = "test123",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            schoolId = "240488",
            phone = "9812345678",
            profileImage = ""
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(true, "Profile loaded successfully", initialUser)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        viewModel.loadUserProfile("test123")

        // Act - Try to update with invalid phone
        viewModel.updateUserProfile("Jane", "Smith", "123")

        // Assert
        assertEquals("Enter valid Nepali phone number", viewModel.message.value)
        // User data should remain unchanged
        assertEquals("John", viewModel.user.value?.firstName)
        assertEquals("Doe", viewModel.user.value?.lastName)
        assertEquals("9812345678", viewModel.user.value?.phone)
    }

    @Test
    fun updateUserProfile_validNepaliPhoneFormats_test() {
        // Arrange
        val initialUser = UserModel(
            uid = "test123",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            schoolId = "240488",
            phone = "9812345678",
            profileImage = ""
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(true, "Profile loaded successfully", initialUser)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        viewModel.loadUserProfile("test123")

        // Test with +977 prefix
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(4)
            callback(true, "Profile updated successfully")
            null
        }.`when`(repo).updateUserProfile(eq("test123"), eq("Jane"), eq("Smith"), eq("+9779812345678"), any())

        // Act
        viewModel.updateUserProfile("Jane", "Smith", "+9779812345678")

        // Assert
        assertEquals("Profile updated successfully", viewModel.message.value)
        assertEquals("+9779812345678", viewModel.user.value?.phone)
    }

    @Test
    fun updateUserProfile_failure_test() {
        // Arrange
        val initialUser = UserModel(
            uid = "test123",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            schoolId = "240488",
            phone = "9812345678",
            profileImage = ""
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(true, "Profile loaded successfully", initialUser)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        viewModel.loadUserProfile("test123")

        // Mock update failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(4)
            callback(false, "Update failed: Network error")
            null
        }.`when`(repo).updateUserProfile(eq("test123"), eq("Jane"), eq("Smith"), eq("9823456789"), any())

        // Act
        viewModel.updateUserProfile("Jane", "Smith", "9823456789")

        // Assert
        assertEquals("Update failed: Network error", viewModel.message.value)
        // User data should remain unchanged on failure
        assertEquals("John", viewModel.user.value?.firstName)
        assertEquals("Doe", viewModel.user.value?.lastName)
        assertEquals("9812345678", viewModel.user.value?.phone)
    }

    @Test
    fun clearMessage_test() {
        // Arrange
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, UserModel?) -> Unit>(1)
            callback(false, "Some error message", null)
            null
        }.`when`(repo).getUserProfile(eq("test123"), any())

        viewModel.loadUserProfile("test123")

        // Act
        viewModel.clearMessage()

        // Assert
        assertEquals("", viewModel.message.value)
    }
}