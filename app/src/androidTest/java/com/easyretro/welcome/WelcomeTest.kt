package com.easyretro.welcome

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easyretro.R
import com.easyretro.common.launchFragmentInHiltContainer
import com.easyretro.ui.welcome.WelcomeFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeTest {

    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Before
    fun setup(){
        launchFragmentInHiltContainer<WelcomeFragment>() {
            navController.setGraph(R.navigation.main_nav_graph)
            Navigation.setViewNavController(this.requireView(), navController)
        }
    }


    @Test
    fun navigateToLoginOnButtonClicked() {
        welcomeRobot {
            waitForButtonsToBeDisplayed()
            clickOnEmailSignIn()
            assertEquals(navController.currentDestination?.id, R.id.navigation_email_account)
        }
    }

    @Test
    fun navigateToSignUpOnButtonClicked() {
        welcomeRobot {
            waitForButtonsToBeDisplayed()
            clickOnSignUp()
            assertEquals(navController.currentDestination?.id, R.id.navigation_email_account)
        }
    }
}