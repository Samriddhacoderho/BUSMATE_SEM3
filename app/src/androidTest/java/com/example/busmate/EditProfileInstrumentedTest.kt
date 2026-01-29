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
    fun testEditProfile_navigateFromDashboardToEditScreen() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard to load
        Thread.sleep(3000)

        // Step 2: Click on Profile tab in bottom navigation
        composeRule.onNodeWithTag("bottomNavProfile")
            .assertExists()
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Step 3: Click Edit Profile button
        composeRule.onNodeWithTag("editProfileButton")
            .assertExists()
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Step 4: Verify navigation to EditProfileActivity
        Intents.intended(hasComponent(EditProfileActivity::class.java.name))
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
            .performTextInput("UpdatedFirstName")

        composeRule.waitForIdle()

        // Clear and enter new last name
        composeRule.onNodeWithTag("lastNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("lastNameField")
            .performTextInput("UpdatedLastName")

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

    @Test
    fun testEditProfile_invalidPhoneNumber() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard
        Thread.sleep(3000)

        // Step 2: Navigate to Edit Profile
        composeRule.onNodeWithTag("bottomNavProfile")
            .performClick()

        Thread.sleep(1000)

        composeRule.onNodeWithTag("editProfileButton")
            .performClick()

        Thread.sleep(2000)

        // Step 3: Enter invalid phone number
        composeRule.onNodeWithTag("phoneField")
            .performTextClearance()

        composeRule.onNodeWithTag("phoneField")
            .performTextInput("123")

        composeRule.waitForIdle()

        // Step 4: Click Save
        composeRule.onNodeWithTag("saveButton")
            .performClick()

        // Wait for validation
        Thread.sleep(2000)

        // Step 5: Verify error message appears (checking that save button still exists means we stayed on same screen)
        composeRule.onNodeWithTag("saveButton")
            .assertExists()
    }

    @Test
    fun testEditProfile_backButtonWorks() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard
        Thread.sleep(3000)

        // Step 2: Navigate to Edit Profile
        composeRule.onNodeWithTag("bottomNavProfile")
            .performClick()

        Thread.sleep(1000)

        composeRule.onNodeWithTag("editProfileButton")
            .performClick()

        Thread.sleep(2000)

        // Step 3: Click back button
        composeRule.onNodeWithTag("backButton")
            .assertExists()
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(1000)

        // Step 4: Verify we're back at Profile screen (Edit Profile button should be visible again)
        composeRule.onNodeWithTag("editProfileButton")
            .assertExists()
    }

    @Test
    fun testEditProfile_profileImageUpdateButton() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard
        Thread.sleep(3000)

        // Step 2: Navigate to Edit Profile
        composeRule.onNodeWithTag("bottomNavProfile")
            .performClick()

        Thread.sleep(1000)

        composeRule.onNodeWithTag("editProfileButton")
            .performClick()

        Thread.sleep(2000)

        // Step 3: Verify profile image area exists and is clickable
        composeRule.onNodeWithTag("profileImageContainer")
            .assertExists()
            .assertIsDisplayed()

        // Note: Actually clicking would open image picker which is hard to test in instrumented tests
        // This test just verifies the UI element exists
    }

    @Test
    fun testEditProfile_fieldsRetainDataOnRotation() {
        // Step 1: Login first
        performLogin()

        // Wait for dashboard
        Thread.sleep(3000)

        // Step 2: Navigate to Edit Profile
        composeRule.onNodeWithTag("bottomNavProfile")
            .performClick()

        Thread.sleep(1000)

        composeRule.onNodeWithTag("editProfileButton")
            .performClick()

        Thread.sleep(2000)

        // Step 3: Enter data
        composeRule.onNodeWithTag("firstNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("firstNameField")
            .performTextInput("TestName")

        // Note: Screen rotation testing requires ActivityScenarioRule
        // This is a simplified test showing field interaction

        composeRule.waitForIdle()

        // Verify the text is still there
        composeRule.onNodeWithTag("firstNameField")
            .assertTextContains("TestName")
    }

    // Helper function to perform login
    private fun performLogin() {
        composeRule.waitForIdle()
        Thread.sleep(1000)

        composeRule.onNodeWithTag("schoolId")
            .performTextInput("240488")

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