package com.easyretro.ui.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountViewModelTest {
    
    private val testCoroutineScope = TestCoroutineScope()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val accountRepository = mock<AccountRepository>()

    private val viewModel = AccountViewModel(repository = accountRepository)

    private val viewStateObserver = mock<Observer<AccountViewState>>()
    private val viewEffectsObserver = mock<Observer<AccountViewEffect>>()

    private val userEmail = "user@email.com"
    private val userPassword = "password"

    @After
    fun `Tear down`() {
        viewModel.viewStates().removeObserver(viewStateObserver)
        viewModel.viewEffects().removeObserver(viewEffectsObserver)
    }

    //region sign in
    @Test
    fun `GIVEN success response AND verified user WHEN signing in THEN open retro list`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.right(UserStatus.VERIFIED))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignIn(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(AccountViewEffect.OpenRetroList)
            }
        }
    }

    @Test
    fun `GIVEN success response AND non verified user WHEN signing in THEN open email verification`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.right(UserStatus.NON_VERIFIED))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignIn(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(AccountViewEffect.OpenEmailVerification)
            }
        }
    }

    @Test
    fun `GIVEN failed response (invalid user) WHEN signing in THEN show specific error message`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.left(Failure.InvalidUserFailure))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignIn(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(
                    AccountViewEffect.ShowUnknownUserSnackBar(
                        FailureMessage.parse(
                            Failure.InvalidUserFailure
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `GIVEN failed response WHEN signing in THEN show generic error message`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signInWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.left(Failure.UnknownError))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignIn(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(
                    AccountViewEffect.ShowGenericSnackBar(
                        FailureMessage.parse(
                            Failure.UnknownError
                        )
                    )
                )
            }
        }
    }
    //endregion

    //region sign up
    @Test
    fun `GIVEN success response WHEN signing up THEN open verification email`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signUpWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.right(Unit))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignUp(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(AccountViewEffect.OpenEmailVerification)
            }
        }
    }

    @Test
    fun `GIVEN failed response (existing user) WHEN signing up THEN show specific snackbar`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signUpWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.left(Failure.UserCollisionFailure))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignUp(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(
                    AccountViewEffect.ShowExistingUserSnackBar(
                        FailureMessage.parse(
                            Failure.UserCollisionFailure
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `GIVEN failed response WHEN signing up THEN show generic snackbar`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signUpWithEmail(email = userEmail, password = userPassword))
                .thenReturn(Either.left(Failure.UnknownError))
            viewModel.viewStates().observeForever(viewStateObserver)
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.SignUp(email = userEmail, password = userPassword))

            inOrder(viewStateObserver, viewEffectsObserver) {
                val viewStateCaptor = argumentCaptor<AccountViewState>()
                verify(viewStateObserver, times(2)).onChanged(viewStateCaptor.capture())
                assertEquals(SigningState.Loading, viewStateCaptor.firstValue.signingState)
                assertEquals(SigningState.RequestDone, viewStateCaptor.secondValue.signingState)

                verify(viewEffectsObserver).onChanged(
                    AccountViewEffect.ShowGenericSnackBar(
                        FailureMessage.parse(
                            Failure.UnknownError
                        )
                    )
                )
            }
        }
    }
    //endregion

    //region reset password
    @Test
    fun `GIVEN reset password viewEvent WHEN user taps reset password THEN open reset password`() {
        testCoroutineScope.launch {
            viewModel.viewEffects().observeForever(viewEffectsObserver)

            viewModel.process(AccountViewEvent.ResetPassword)

            verify(viewEffectsObserver).onChanged(AccountViewEffect.OpenResetPassword)
        }
    }
    //endregion
}