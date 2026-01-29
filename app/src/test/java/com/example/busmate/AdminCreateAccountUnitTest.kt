package com.example.busmate

import com.example.busmate.data.UserRepositoryInterface
import com.example.busmate.model.CreateAccountModel
import com.example.busmate.viewmodel.CreateAccountViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * Unit tests for Create Account functionality
 * Tests the CreateAccountViewModel's account creation methods
 */
class AdminCreateAccountUnitTest {

    private lateinit var repo: UserRepositoryInterface
    private lateinit var viewModel: CreateAccountViewModel

    @Before
    fun setup() {
        repo = mock()
        viewModel = CreateAccountViewModel(repo)
    }

    /**
     * Test 1: Successfully create a Driver account
     * Verifies that driver account creation works correctly
     */
    @Test
    fun createDriverAccount_success_test() {
        // Arrange
        val role = "Driver"
        val schoolId = "DRV001"

        // Mock the repository response
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String, Boolean) -> Unit>(1)
            callback("Created Account Successful", true)
            null
        }.`when`(repo).createAccount(any(), any())

        // Act
        viewModel.createAccountWithMinimalData(role, schoolId)

        // Assert
        assertEquals("Created Account Successful", viewModel.message.value)

        // Verify the repo was called with correct data
        verify(repo).createAccount(
            eq(CreateAccountModel(role = role, schoolId = schoolId)),
            any()
        )
    }

    /**
     * Test 2: Successfully create a Parent account
     * Verifies that parent account creation works correctly
     */
    @Test
    fun createParentAccount_success_test() {
        // Arrange
        val role = "Parent"
        val schoolId = "240488"

        // Mock the repository response
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String, Boolean) -> Unit>(1)
            callback("Created Account Successful", true)
            null
        }.`when`(repo).createAccount(any(), any())

        // Act
        viewModel.createAccountWithMinimalData(role, schoolId)

        // Assert
        assertEquals("Created Account Successful", viewModel.message.value)

        // Verify the repo was called
        verify(repo).createAccount(
            eq(CreateAccountModel(role = role, schoolId = schoolId)),
            any()
        )
    }

    /**
     * Test 3: Account creation failure
     * Verifies that account creation failures are handled correctly
     */
    @Test
    fun createAccount_failure_test() {
        // Arrange
        val role = "Driver"
        val schoolId = "DRV001"

        // Mock the repository failure response
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String, Boolean) -> Unit>(1)
            callback("Account already exists", false)
            null
        }.`when`(repo).createAccount(any(), any())

        // Act
        viewModel.createAccountWithMinimalData(role, schoolId)

        // Assert
        assertEquals("Account already exists", viewModel.message.value)

        // Verify the repo was called
        verify(repo).createAccount(
            eq(CreateAccountModel(role = role, schoolId = schoolId)),
            any()
        )
    }

    /**
     * Test 4: Empty role
     * Verifies that empty role behavior is handled correctly
     */
    @Test
    fun createAccount_emptyRole_test() {
        // Arrange
        val role = ""
        val schoolId = "DRV001"

        // Mock the repository
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String, Boolean) -> Unit>(1)
            callback("Created Account Successful", true)
            null
        }.`when`(repo).createAccount(any(), any())

        // Act
        viewModel.createAccountWithMinimalData(role, schoolId)

        // Assert - Should still attempt to create (validation is in UI)
        assertEquals("Created Account Successful", viewModel.message.value)
    }

    /**
     * Test 9: Empty school ID handling
     * Verifies behavior when empty school ID is provided
     */
    @Test
    fun createAccount_emptySchoolId_test() {
        // Arrange
        val role = "Driver"
        val schoolId = ""

        // Mock the repository
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String, Boolean) -> Unit>(1)
            callback("Created Account Successful", true)
            null
        }.`when`(repo).createAccount(any(), any())

        // Act
        viewModel.createAccountWithMinimalData(role, schoolId)

        // Assert - Should still attempt to create (validation is in UI)
        assertEquals("Created Account Successful", viewModel.message.value)
    }
}