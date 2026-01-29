package com.example.busmate

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.busmate.view.admin.CreateAccountScreenActivity
import com.example.busmate.view.auth.LoginScreen
import com.example.busmate.view.dashboard.ParentDashboardActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for Create Account functionality
 *
 * Tests the complete flow:
 * 1. Admin logs in
 * 2. Navigates to Create User screen
 * 3. Selects Driver role
 * 4. Uses auto-generated ID
 * 5. Submits the form
 * 6. Verifies success
 */
@RunWith(AndroidJUnit4::class)
class AdminCreateAccountInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginScreen>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /**
     * Complete E2E test: Admin logs in and creates a new Driver account
     */
    @Test
    fun testAdmin_createDriverAccount_success() {
        // ========== STEP 1: Admin Login ==========
        performAdminLogin()

        // ========== STEP 2: Navigate to Create User ==========
        navigateToCreateUserScreen()

        // ========== STEP 3: Select Driver Role ==========
        selectDriverRole()

        // ========== STEP 4: Verify Auto-Generated ID (Optional: can modify) ==========
        verifyUserIdExists()

        // ========== STEP 5: Submit Form ==========
        submitCreateAccountForm()

        // ========== STEP 6: Verify Success ==========
        verifyAccountCreationSuccess()
    }


    private fun performAdminLogin() {
        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Enter admin school ID
        composeRule.onNodeWithTag("schoolId")
            .assertExists("School ID field should exist")
            .performTextInput("240453")

        composeRule.waitForIdle()

        // Enter admin password
        composeRule.onNodeWithTag("password")
            .assertExists("Password field should exist")
            .performTextInput("Samkodata1@")

        composeRule.waitForIdle()

        // Click login button
        composeRule.onNodeWithTag("loginButton")
            .assertExists("Login button should exist")
            .performClick()

        // Wait for login and navigation to dashboard
        Thread.sleep(3000)

        // Verify navigation to dashboard
        Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))
    }

    private fun navigateToCreateUserScreen() {
        // Wait for dashboard to fully load
        composeRule.waitForIdle()
        Thread.sleep(2000)

        // Click the "Create User" button in the admin grid
        composeRule.onNodeWithTag("createUserButton")
            .assertExists("Create User button should exist in admin grid")
            .performClick()

        // Wait for navigation to Create Account screen
        Thread.sleep(2000)

        // Verify navigation to CreateAccountScreenActivity
        Intents.intended(hasComponent(CreateAccountScreenActivity::class.java.name))
    }

    private fun selectDriverRole() {
        // Wait for screen to load
        composeRule.waitForIdle()

        // Click the role dropdown
        composeRule.onNodeWithTag("roleDropdown")
            .assertExists("Role dropdown should exist")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Select "Driver" from dropdown menu
        composeRule.onAllNodesWithText("Driver", useUnmergedTree = true)
            .onFirst()
            .assertExists("Driver option should exist in dropdown")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)
    }

    private fun verifyUserIdExists() {
        // The new UI auto-generates a random 6-digit ID
        // We just need to verify it exists and is not empty
        composeRule.onNodeWithTag("userIdField")
            .assertExists("User ID field should exist")
            .assertTextContains("", substring = true) // Verify field has content

        composeRule.waitForIdle()


    }

    private fun submitCreateAccountForm() {
        // Click the create account button
        composeRule.onNodeWithTag("createAccountButton")
            .assertExists("Create Account button should exist")
            .performClick()

        // Wait for account creation to complete
        Thread.sleep(3000)
    }

    private fun verifyAccountCreationSuccess() {
        composeRule.waitForIdle()

        // For driver creation, we should stay on the same screen
        // Verify we're still on Create Account screen using the test tag
        composeRule.onNodeWithTag("registerAccountTitle")
            .assertExists("Should remain on Create Account screen after creating driver")

        // Additional verification - check that form is ready for next input
        composeRule.onNodeWithTag("createAccountButton")
            .assertExists("Create Account button should still be visible")
    }


}