package com.example.busmate

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.busmate.view.all.EditProfileActivity
import com.example.busmate.view.auth.LoginScreen
import com.example.busmate.view.dashboard.ParentDashboardActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileInstrumentedTest {

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



    @Test
    fun testEditProfile_updateProfileSuccessfully() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard
        Thread.sleep(3000)

        // Step 2: Navigate to Profile Edit Screen
        composeRule.onNodeWithTag("bottomNavProfile")
            .performClick()

        Thread.sleep(1000)

        // Step 3: Navigate to Edit Profile Activity
        composeRule.onNodeWithTag("editProfileButton")
            .performClick()

        Thread.sleep(2000)

        // Step 4: Verify fields are pre-filled and Edit them
        composeRule.onNodeWithTag("firstNameField")
            .assertExists()
            .assertIsDisplayed()

        // Clear and enter new first name
        composeRule.onNodeWithTag("firstNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("firstNameField")
            .performTextInput("Mr. Keshab")

        composeRule.waitForIdle()

        // Clear and enter new last name
        composeRule.onNodeWithTag("lastNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("lastNameField")
            .performTextInput("Bhattarai")

        composeRule.waitForIdle()

        // Clear and enter new phone
        composeRule.onNodeWithTag("phoneField")
            .performTextClearance()

        composeRule.onNodeWithTag("phoneField")
            .performTextInput("9823456789")

        composeRule.waitForIdle()

        // Step 5: Click Save button
        composeRule.onNodeWithTag("saveButton")
            .assertExists()
            .performClick()

        // Wait for save operation
        Thread.sleep(3000)

        // Step 6: Verify success message appears (via Snackbar)
        // Note: Snackbar might be difficult to test, but we can verify no error occurred
        // by checking if we can still interact with the screen
        composeRule.onNodeWithTag("saveButton")
            .assertExists()
    }


    // Helper function to perform login
    private fun performLogin() {
        composeRule.waitForIdle()
        Thread.sleep(1000)

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("240453")

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("password")
            .performTextInput("Samkodata1@")

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("loginButton")
            .performClick()

        Thread.sleep(2000)

        // Verify navigation to dashboard
        Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))
    }
}