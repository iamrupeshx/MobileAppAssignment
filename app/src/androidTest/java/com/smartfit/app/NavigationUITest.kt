package com.smartfit.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Automated UI tests for SmartFit navigation and screen display.
 * Every group member must add at least 2 UI tests.
 */
@RunWith(AndroidJUnit4::class)
class NavigationUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ── Member 1: Auth screen tests ───────────────────────────────────────────

    @Test
    fun splashScreen_displaysAppName() {
        // App should show SmartFit text on splash
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("SMARTFIT").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("SMARTFIT").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysWelcomeText() {
        // Wait for navigation from splash to login
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Welcome Back").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    @Test
    fun loginScreen_hasEmailAndPasswordFields() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Email Address").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Email Address").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
    }

    @Test
    fun loginScreen_hasSignInButton() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("SIGN IN").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("SIGN IN").assertIsDisplayed()
    }

    // ── Member 2: Registration navigation tests ──────────────────────────────

    @Test
    fun clickRegisterHere_navigatesToRegisterScreen() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Register Here").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Register Here").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Create Account").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registerScreen_hasAllRequiredFields() {
        // Navigate to register
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Register Here").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Register Here").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Full Name").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Full Name").assertExists()
        composeTestRule.onNodeWithText("Email Address").assertExists()
        composeTestRule.onNodeWithText("Age").assertExists()
    }

    // ── Member 3: Form validation tests ──────────────────────────────────────

    @Test
    fun loginScreen_emptyEmail_showsError() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("SIGN IN").fetchSemanticsNodes().isNotEmpty()
        }
        // Click login without filling in fields
        composeTestRule.onNodeWithText("SIGN IN").performClick()

        // Should show error from AuthViewModel
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Please enter your email").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Please enter your email").assertIsDisplayed()
    }

    @Test
    fun loginScreen_invalidEmail_showsFormatError() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Email Address").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Email Address").performTextInput("invalid-email")
        composeTestRule.onNodeWithText("SIGN IN").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Please enter a valid email").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Please enter a valid email").assertIsDisplayed()
    }

    // ── Member 4: UI content tests ────────────────────────────────────────────

    @Test
    fun loginScreen_hasTaglineText() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Sign in to continue your journey")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Sign in to continue your journey").assertIsDisplayed()
    }

    @Test
    fun registerScreen_hasCreateAccountButton() {
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText("Register Here").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Register Here").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("CREATE ACCOUNT").fetchSemanticsNodes().isNotEmpty()
        }
        // Added performScrollTo() because the button is at the bottom of a long scrollable screen
        composeTestRule.onNodeWithText("CREATE ACCOUNT").performScrollTo().assertIsDisplayed()
    }
}
