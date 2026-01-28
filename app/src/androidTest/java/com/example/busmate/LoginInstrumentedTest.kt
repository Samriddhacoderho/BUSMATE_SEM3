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
    fun testSuccessfulLogin_navigatesToParentDashboard() {
        // Wait for the screen to be fully loaded
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

        // Wait for Firebase authentication and navigation
        // Reduced to 2 seconds to catch navigation before permission dialog
        Thread.sleep(2000)

        Intents.intended(hasComponent(ParentDashboardActivity::class.java.name))
    }
}