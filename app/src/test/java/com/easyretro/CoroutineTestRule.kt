package com.easyretro

import com.easyretro.common.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutineTestRule(val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {

    val testDispatcherProvider = object : CoroutineDispatcherProvider {
        override fun io(): CoroutineDispatcher = testDispatcher
        override fun main(): CoroutineDispatcher = testDispatcher
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}