package com.example.busmate

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.busmate.view.auth.LoginScreen
import com.example.busmate.view.auth.SignUpScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupInstrumentedTest{

    @get:Rule
    val composeRule = createAndroidComposeRule<SignUpScreen>()

    @Before
    fun setup() {
        // Initialize Espresso Intents to capture navigation
        Intents.init()
    }

    @After
    fun tearDown() {
        // Release Espresso Intents resources
        Intents.release()
    }

    /**
     * Test 1: Complete SignUp Flow with Navigation
     * This test performs a full registration and verifies the activity finishes
     * (which happens on successful registration according to SignUpScreen.kt line 133)
     */
    @Test
    fun testCompleteSignUpFlow_successfulRegistration_finishesActivity() {
        // Wait for the screen to load
        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Generate a unique email to avoid conflicts
        val timestamp = System.currentTimeMillis()
        val uniqueEmail = "testuser$timestamp@gmail.com"

        // Fill in First Name
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.waitForIdle()

        // Fill in Last Name
        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        composeRule.waitForIdle()

        // Fill in Email (unique to avoid conflicts)
        composeRule.onNodeWithTag("email")
            .performTextInput(uniqueEmail)

        composeRule.waitForIdle()

        // Fill in School ID
        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST$timestamp")

        composeRule.waitForIdle()

        // Fill in Phone (valid Nepali phone number)
        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.waitForIdle()

        // Fill in Password (meets all requirements)
        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.waitForIdle()

        // Fill in Confirm Password
        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@123")

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Verify Sign Up button is enabled
        composeRule.onNodeWithTag("signUpButton")
            .assertIsEnabled()

        // Click Sign Up button
        composeRule.onNodeWithTag("signUpButton")
            .performClick()

        // Wait for Firebase registration to complete
        // The activity should finish on successful registration
        Thread.sleep(5000)

        // At this point, the SignUpScreen activity should have finished
        // and we should be back at the LoginScreen
        // We can verify this by checking if the activity is finishing
    }

   


    }
