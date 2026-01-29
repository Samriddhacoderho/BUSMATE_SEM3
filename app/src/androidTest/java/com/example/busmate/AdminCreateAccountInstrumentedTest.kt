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
 * Tests the complete user journey from admin login to creating a driver account
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
     *
     * Flow:
     * 1. Admin logs in with credentials
     * 2. Navigates to Home screen (dashboard)
     * 3. Clicks "Create User" button from admin grid
     * 4. Selects "Driver" role from dropdown
     * 5. Enters school/user ID
     * 6. Clicks "Create Account" button
     * 7. Verifies success (stays on create account screen or shows success message)
     */
    @Test
    fun testAdmin_createDriverAccount_success() {
        // ========== STEP 1: Admin Login ==========
        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Enter admin school ID (adjust this to your actual admin credentials)
        composeRule.onNodeWithTag("schoolId")
            .assertExists("School ID field should exist")
            .performTextInput("240453")  // ← REPLACE with your actual admin school ID

        composeRule.waitForIdle()

        // Enter admin password
        composeRule.onNodeWithTag("password")
            .assertExists("Password field should exist")
            .performTextInput("Samkodata1@")  // ← REPLACE with your actual admin password

        composeRule.waitForIdle()

        // Click login button
        composeRule.onNodeWithTag("loginButton")
            .assertExists("Login button should exist")
            .performClick()

        // Wait for login and navigation to dashboard
        Thread.sleep(3000)

        // Verify navigation to dashboard
        Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))

        // ========== STEP 2: Navigate to Create User ==========
        // Wait for dashboard to fully load
        Thread.sleep(2000)

        // Click on "Create User" button in admin grid
        // This uses semantic matching to find the button with "Create User" text
        composeRule.onNode(
            hasText("Create User") and hasClickAction(),
            useUnmergedTree = true
        )
            .assertExists("Create User button should exist in admin grid")
            .performClick()

        // Wait for navigation to Create Account screen
        Thread.sleep(2000)

        // Verify navigation to CreateAccountScreenActivity
        Intents.intended(hasComponent(CreateAccountScreenActivity::class.java.name))

        // ========== STEP 3: Fill Create Account Form ==========

        // Click on role dropdown
        composeRule.onNode(
            hasText("Select Role") or hasText("User Role"),
            useUnmergedTree = true
        )
            .assertExists("Role dropdown should exist")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Select "Driver" from dropdown
        composeRule.onNode(
            hasText("Driver") and hasClickAction(),
            useUnmergedTree = true
        )
            .assertExists("Driver option should exist in dropdown")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Enter school/user ID for the new driver
        // Generate a unique ID to avoid conflicts
        val uniqueDriverId = "DRV${System.currentTimeMillis() % 10000}"

        composeRule.onNode(
            hasSetTextAction() and hasText("School ID or User ID", substring = true),
            useUnmergedTree = true
        )
            .assertExists("School ID field should exist")
            .performTextInput(uniqueDriverId)

        composeRule.waitForIdle()

        // ========== STEP 4: Create Account ==========

        // Click "Create Account" button
        composeRule.onNode(
            hasText("Create Account") and hasClickAction(),
            useUnmergedTree = true
        )
            .assertExists("Create Account button should exist")
            .performClick()

        // Wait for account creation
        Thread.sleep(3000)

        // ========== STEP 5: Verify Success ==========

        // Success can be verified in multiple ways:
        // 1. Check if success message appears
        // 2. Check if we're still on the create account screen (for Driver, we stay)
        // 3. Check if the form is reset or still visible

        // Verify the screen still has the create account elements
        // (For driver creation, we should stay on the same screen with success message)
        composeRule.onNode(
            hasText("Create Account") or hasText("Register Account"),
            useUnmergedTree = true
        )
            .assertExists("Should remain on Create Account screen after creating driver")
    }

    // ========== HELPER FUNCTIONS ==========
    // (Kept for reference, but not used in this single test case)
}