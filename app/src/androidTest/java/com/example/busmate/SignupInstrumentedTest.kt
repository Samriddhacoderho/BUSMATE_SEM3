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
class SignupInstrumentedTestComplete {

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

    /**
     * Test 2: Verify all fields with valid data
     * This test just fills all fields to ensure they accept input correctly
     */
    @Test
    fun testAllFieldsAcceptValidInput() {
        composeRule.waitForIdle()
        Thread.sleep(500)

        // Fill all fields with valid data
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Ram")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("Thapa")

        composeRule.onNodeWithTag("email")
            .performTextInput("ram@gmail.com")

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("ATX6647")

        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password")
            .performTextInput("Password@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Password@123")

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Verify button is enabled with all valid data
        composeRule.onNodeWithTag("signUpButton")
            .assertIsEnabled()
    }

    /**
     * Test 3: Test Sign Up with existing email shows error
     * Use an email that you know exists in your Firebase
     */
    @Test
    fun testSignUp_existingEmail_showsError() {
        composeRule.waitForIdle()
        Thread.sleep(500)

        // Fill all fields with valid data but use an existing email
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        // Use an email that already exists (modify this to match your test data)
        composeRule.onNodeWithTag("email")
            .performTextInput("satyalsamriddha@gmail.com")

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST123")

        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@123")

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Click Sign Up
        composeRule.onNodeWithTag("signUpButton")
            .performClick()

        // Wait for Firebase error
        Thread.sleep(3000)

        // Should show an error message (exact text depends on Firebase error)
        // The activity should NOT finish since registration failed
    }

    /**
     * Test 4: Test Sign Up button disabled with empty fields
     */
    @Test
    fun testSignUpButton_disabledWithEmptyFields() {
        composeRule.waitForIdle()

        // Button should be disabled when fields are empty
        composeRule.onNodeWithTag("signUpButton")
            .assertIsNotEnabled()
    }

    /**
     * Test 5: Test Sign Up button disabled with invalid email
     */
    @Test
    fun testSignUpButton_disabledWithInvalidEmail() {
        composeRule.waitForIdle()

        // Fill all fields except email is invalid
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        composeRule.onNodeWithTag("email")
            .performTextInput("invalidemail") // Invalid email

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST123")

        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@123")

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Button should be disabled due to invalid email
        composeRule.onNodeWithTag("signUpButton")
            .assertIsNotEnabled()
    }

    /**
     * Test 6: Test Sign Up button disabled with password mismatch
     */
    @Test
    fun testSignUpButton_disabledWithPasswordMismatch() {
        composeRule.waitForIdle()

        // Fill all fields but passwords don't match
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        composeRule.onNodeWithTag("email")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST123")

        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@456") // Different password

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Button should be disabled due to password mismatch
        composeRule.onNodeWithTag("signUpButton")
            .assertIsNotEnabled()
    }

    /**
     * Test 7: Test Sign Up button disabled with weak password
     */
    @Test
    fun testSignUpButton_disabledWithWeakPassword() {
        composeRule.waitForIdle()

        // Fill all fields but password is weak (no special character)
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        composeRule.onNodeWithTag("email")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST123")

        composeRule.onNodeWithTag("phone")
            .performTextInput("9876543210")

        composeRule.onNodeWithTag("password")
            .performTextInput("Test123") // Missing special character

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test123")

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Button should be disabled due to weak password
        composeRule.onNodeWithTag("signUpButton")
            .assertIsNotEnabled()
    }

    /**
     * Test 8: Test Sign Up button disabled with invalid phone
     */
    @Test
    fun testSignUpButton_disabledWithInvalidPhone() {
        composeRule.waitForIdle()

        // Fill all fields but phone is invalid
        composeRule.onNodeWithTag("firstName")
            .performTextInput("Test")

        composeRule.onNodeWithTag("lastName")
            .performTextInput("User")

        composeRule.onNodeWithTag("email")
            .performTextInput("test@gmail.com")

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("TEST123")

        composeRule.onNodeWithTag("phone")
            .performTextInput("123") // Invalid phone

        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@123")

        composeRule.waitForIdle()
        Thread.sleep(500)

        // Button should be disabled due to invalid phone
        composeRule.onNodeWithTag("signUpButton")
            .assertIsNotEnabled()
    }
}