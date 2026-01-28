package com.example.busmate

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.busmate.view.auth.LoginScreen
import com.example.busmate.view.dashboard.ParentDashboardActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginScreen>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun successfulLogin_navigatesToDashboard() {
        // Input school ID
        composeTestRule.onNodeWithTag("schoolId")
            .performTextInput("240453")

        // Input password
        composeTestRule.onNodeWithTag("password")
            .performTextInput("Samkodata1@")

        // Click login button
        composeTestRule.onNodeWithTag("loginButton")
            .performClick()

        // Use waitUntil to check for the intent before activity finishes
        // This gives time for async login and catches the intent before activity.finish()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                // Try to verify intent was sent
                Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))
                true // Intent was found, test passes
            } catch (e: AssertionError) {
                // Intent not found yet, keep waiting
                false
            }
        }
    }
}