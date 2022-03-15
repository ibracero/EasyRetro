package com.easyretro.common

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import org.hamcrest.Matcher
import timber.log.Timber

open class BaseRobot {

    companion object {
        const val DEFAULT_WAIT_TIMEOUT = 10000L
        private const val DEFAULT_WAIT_POLLING_TIME = 500L
    }

    fun waitForView(
        viewMatcher: Matcher<View>,
        viewAssertion: ViewAssertion,
        crashOnTimeout: Boolean = true,
        waitTimeout: Long = DEFAULT_WAIT_TIMEOUT,
        waitPollingTime: Long = DEFAULT_WAIT_POLLING_TIME
    ): Boolean {
        val endTime = System.currentTimeMillis() + waitTimeout
        var isIdle = false
        while (!isIdle) {
            isIdle = try {
                Espresso.onView(viewMatcher).check(viewAssertion)
                true
            } catch (e: NoMatchingViewException) {
                false
            } catch (e: Throwable) {
                Timber.e("Error while waiting for view '$viewMatcher'\n$e")
                false
            }
            if (System.currentTimeMillis() > endTime) {
                if (crashOnTimeout)
                    throw UiException("Waiting for view '$viewMatcher' with condition '$viewAssertion' timed out")
                else {
                    Timber.d("Waiting for view '$viewMatcher' with condition '$viewAssertion' timed out. Resuming tests...")
                    return false
                }
            }
            Thread.sleep(waitPollingTime)
        }
        Timber.d("Waiting for view '$viewMatcher' with condition '$viewAssertion' finished.")
        return true
    }
}