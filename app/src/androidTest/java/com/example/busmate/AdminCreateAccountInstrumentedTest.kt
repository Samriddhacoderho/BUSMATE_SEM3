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

    @Test
    fun testAdmin_createDriverAccount_success() {
        performAdminLogin()
        navigateToCreateUserScreen()
        selectDriverRole()
        enterUniqueDriverId()
        submitCreateAccountForm()
        verifyAccountCreationSuccess()
    }

    private fun performAdminLogin() {
        composeRule.waitForIdle()
        Thread.sleep(1000)

        composeRule.onNodeWithTag("schoolId")
            .assertExists("School ID field should exist")
            .performTextInput("240453")

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("password")
            .assertExists("Password field should exist")
            .performTextInput("Samkodata1@")

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("loginButton")
            .assertExists("Login button should exist")
            .performClick()

        Thread.sleep(3000)
        Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))
    }

    private fun navigateToCreateUserScreen() {
        composeRule.waitForIdle()
        Thread.sleep(2000)

        composeRule.onNodeWithTag("createUserButton")
            .assertExists("Create User button should exist in admin grid")
            .performClick()

        Thread.sleep(2000)
        Intents.intended(hasComponent(CreateAccountScreenActivity::class.java.name))
    }

    private fun selectDriverRole() {
        composeRule.onNodeWithTag("roleDropdown")
            .assertExists("Role dropdown should exist")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)

        composeRule.onAllNodesWithText("Driver", useUnmergedTree = true)
            .onFirst()
            .assertExists("Driver option should exist in dropdown")
            .performClick()

        composeRule.waitForIdle()
        Thread.sleep(500)
    }

    private fun enterUniqueDriverId() {
        val uniqueDriverId = "DRV${System.currentTimeMillis() % 10000}"

        composeRule.onNodeWithTag("userIdField")
            .assertExists("User ID field should exist")
            .performTextInput(uniqueDriverId)

        composeRule.waitForIdle()
    }

    private fun submitCreateAccountForm() {
        composeRule.onNodeWithTag("createAccountButton")
            .assertExists("Create Account button should exist")
            .performClick()

        Thread.sleep(3000)
    }

    private fun verifyAccountCreationSuccess() {
        composeRule.waitForIdle()

        // Verify we're still on Create Account screen using the test tag
        composeRule.onNodeWithTag("registerAccountTitle")
            .assertExists("Should remain on Create Account screen after creating driver")

        // Optional: Additional verification - check that form is ready for next input
        composeRule.onNodeWithTag("createAccountButton")
            .assertExists("Create Account button should still be visible")
    }
}