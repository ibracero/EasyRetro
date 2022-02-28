package com.easyretro.ui.welcome

import app.cash.turbine.test
import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.welcome.WelcomeContract.*
import com.easyretro.ui.FailureMessage
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@ExperimentalCoroutinesApi
class WelcomeViewModelTest {
    private val accountRepository = mock<AccountRepository>()

    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = mock<CoroutineDispatcherProvider> {
        on { main() }.then { dispatcher }
        on { io() }.then { dispatcher }
    }

    private val viewModel = WelcomeViewModel(repository = accountRepository, dispatchers)

    private val idToken = "token"
    private val userEmail = "user@email.com"
    private val givenName = "givenName"
    private val familyName = "familyName"
    private val user = User(email = userEmail, firstName = givenName, lastName = familyName, photoUrl = "")
    private val googleAccount = mock<GoogleSignInAccount>() {
        on { idToken }.thenReturn(idToken)
        on { email }.thenReturn(userEmail)
        on { givenName }.thenReturn(givenName)
        on { familyName }.thenReturn(familyName)
        on { photoUrl }.thenReturn(null)
    }

    @Test
    fun `GIVEN user session already started WHEN welcome screen finishes loading THEN navigate to retro list`() =
        runTest {
            whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.VERIFIED))

            viewModel.viewStates().test {
                val state = awaitItem()
                assertFalse(state.areLoginButtonsShown)
                assertFalse(state.isLoadingShown)
                cancelAndConsumeRemainingEvents()
            }

            viewModel.viewEffects().test {
                viewModel.process(Event.ScreenLoaded)
                assertEquals(Effect.NavigateToRetros, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `GIVEN user session not started WHEN welcome screen finishes loading THEN show buttons`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewStates().test {
            val splashState = awaitItem()
            assertFalse(splashState.areLoginButtonsShown)
            assertFalse(splashState.isLoadingShown)
            viewModel.process(Event.ScreenLoaded)
            val buttonsState = awaitItem()
            assertTrue(buttonsState.areLoginButtonsShown)
            assertFalse(buttonsState.isLoadingShown)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `WHEN google sign in clicked THEN open google sign in flow`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewEffects().test {
            viewModel.process(Event.GoogleSignInClicked)

            assertEquals(Effect.NavigateToGoogleSignIn, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `WHEN email sign in clicked THEN navigate to login screen`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewEffects().test {
            viewModel.process(Event.EmailSignInClicked)

            assertEquals(Effect.NavigateToEmailLogin, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `WHEN sign up clicked THEN navigate to sign up screen`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewEffects().test {
            viewModel.process(Event.SignUpClicked)

            assertEquals(Effect.NavigateToSignUp, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN successful response WHEN logging in with Google THEN navigate to retro list`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))
        whenever(accountRepository.signWithGoogleAccount(idToken, user)).thenReturn(Either.right(Unit))

        viewModel.viewEffects().test {
            viewModel.process(Event.GoogleSignInResultReceived(googleAccount))

            assertEquals(Effect.NavigateToRetros, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `GIVEN error response WHEN logging in with Google THEN navigate to retro list`() = runTest {
        whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))

        viewModel.viewEffects().test {
            viewModel.process(Event.GoogleSignInResultReceived(null))

            assertEquals(Effect.GoogleSignInError(FailureMessage.parse(Failure.UnknownError)), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}