package com.easyretro.welcome

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.easyretro.R
import com.easyretro.common.BaseRobot
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf

fun welcomeRobot(func: WelcomeRobot.() -> Unit) = WelcomeRobot().apply { func() }

class WelcomeRobot : BaseRobot() {

    fun waitForButtonsToBeDisplayed() {
        waitForView(withId(R.id.email_sign_in), matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    fun clickOnGoogleSignIn() {
        onView(anyOf(withId(R.id.google_sign_in))).perform(click())
    }

    fun clickOnEmailSignIn() {
        onView(withId(R.id.email_sign_in)).perform(click())
    }

    fun clickOnSignUp() {
        onView(withId(R.id.sign_up_button)).perform(click())
    }
}