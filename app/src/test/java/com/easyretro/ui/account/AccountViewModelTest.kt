package com.easyretro.ui.account

import app.cash.turbine.test
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.CoroutinesTestRule
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.account.AccountContract.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private val accountRepository = mock<AccountRepository>()

    private val viewModel = AccountViewModel(repository = accountRepository)

    private val userEmail = "user@email.com"
    private val userPassword = "password"

    @Test
    fun `GIVEN screen loaded event WHEN signing up THEN show sign up form`() = runTest {

        viewModel.viewStates().test {
            viewModel.process(Event.ScreenLoaded(isNewAccount = true))

            val state = expectMostRecentItem().formState
            assertEquals(FormState.SignUpForm, state)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN screen loaded event WHEN signing in THEN show sign in form`() = runTest {

        viewModel.viewStates().test {
            viewModel.process(Event.ScreenLoaded(isNewAccount = false))

            val state = expectMostRecentItem().formState
            assertEquals(FormState.SignInForm, state)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN success response AND verified user WHEN signing in THEN open retro list`() = runTest {
        whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
            .thenReturn(Either.right(UserStatus.VERIFIED))

        viewModel.viewEffects().test() {
            viewModel.process(Event.SignInClicked(email = userEmail, password = userPassword))

            assertEquals(Effect.OpenRetroList, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN success response AND non verified user WHEN signing in THEN open email verification`() = runTest {
        whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
            .thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewEffects().test() {
            viewModel.process(Event.SignInClicked(email = userEmail, password = userPassword))

            assertEquals(Effect.OpenEmailVerification, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response (invalid user) WHEN signing in THEN show specific error message`() = runTest {
        whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
            .thenReturn(Either.left(Failure.UnknownError))

        viewModel.viewEffects().test() {
            viewModel.process(Event.SignInClicked(email = userEmail, password = userPassword))

            val errorMessage = FailureMessage.parse(Failure.UnknownError)
            assertEquals(Effect.ShowGenericError(errorMessage), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN success response WHEN signing up THEN open email verification`() = runTest {
        whenever(accountRepository.signUpWithEmail(email = userEmail, password = userPassword))
            .thenReturn(Either.right(Unit))

        viewModel.viewEffects().test() {
            viewModel.process(Event.SignUpClicked(email = userEmail, password = userPassword))

            assertEquals(Effect.OpenEmailVerification, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN failed response  WHEN signing up THEN show specific error message`() = runTest {
        whenever(accountRepository.signUpWithEmail(email = userEmail, password = userPassword))
            .thenReturn(Either.left(Failure.UnknownError))

        viewModel.viewEffects().test() {
            viewModel.process(Event.SignUpClicked(email = userEmail, password = userPassword))

            val errorMessage = FailureMessage.parse(Failure.UnknownError)
            assertEquals(Effect.ShowGenericError(errorMessage), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN reset password uiEvent WHEN user taps reset password THEN open reset password`() = runTest {

        viewModel.viewEffects().test() {
            viewModel.process(Event.ResetPasswordClicked)

            assertEquals(Effect.OpenResetPassword, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN sign in uiEvent WHEN user taps snackbar THEN show sign in form`() = runTest {

        viewModel.viewStates().test() {
            viewModel.process(Event.SnackBarSignInClicked)

            assertEquals(FormState.SignInForm, expectMostRecentItem().formState)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN sign up uiEvent WHEN user taps snackbar THEN show sign up form`() = runTest {

        viewModel.viewStates().test() {
            viewModel.process(Event.SnackBarSignUpClicked)

            assertEquals(FormState.SignUpForm, expectMostRecentItem().formState)
            cancelAndConsumeRemainingEvents()
        }
    }
}