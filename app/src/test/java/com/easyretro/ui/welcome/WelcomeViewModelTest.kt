package com.easyretro.ui.welcome

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WelcomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val accountRepository = mock<AccountRepository>()

    private val testCoroutineScope = TestCoroutineScope()
    
    private lateinit var viewModel: WelcomeViewModel

    private val userSessionObserver = mock<Observer<Either<Failure, UserStatus>>>()
    private val googleSignInObserver = mock<Observer<Either<Failure, Unit>>>()

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

    //region welcome launch
    @Test
    fun `GIVEN user verified WHEN welcome page launches THEN return VERIFIED to view`() {
        testCoroutineScope.launch {
            whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.VERIFIED))
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.userSessionLiveData.observeForever(userSessionObserver)

            verify(userSessionObserver).onChanged(Either.right(UserStatus.VERIFIED))
            verifyNoMoreInteractions(userSessionObserver)
        }
    }

    @Test
    fun `GIVEN user non verified WHEN welcome page launches THEN pass NON VERIFIED to view`() {
        testCoroutineScope.launch {
            whenever(accountRepository.getUserStatus()).thenReturn(Either.right(UserStatus.NON_VERIFIED))
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.userSessionLiveData.observeForever(userSessionObserver)

            verify(userSessionObserver).onChanged(Either.right(UserStatus.NON_VERIFIED))
            verifyNoMoreInteractions(userSessionObserver)
        }
    }

    @Test
    fun `GIVEN failed response WHEN welcome page launches THEN pass the error to view`() {
        testCoroutineScope.launch {
            whenever(accountRepository.getUserStatus()).thenReturn(Either.left(Failure.UnknownError))
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.userSessionLiveData.observeForever(userSessionObserver)

            verify(userSessionObserver).onChanged(Either.left(Failure.UnknownError))
            verifyNoMoreInteractions(userSessionObserver)
        }
    }
    //endregion

    //region sign in with google
    @Test
    fun `GIVEN success response WHEN logging in with Google THEN pass success to view`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signWithGoogleAccount(idToken, user)).thenReturn(Either.right(Unit))
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.googleSignInLiveData.observeForever(googleSignInObserver)

            viewModel.handleSignInResult(googleAccount)

            verify(googleSignInObserver).onChanged(Either.right(Unit))
            verifyNoMoreInteractions(googleSignInObserver)
        }
    }

    @Test
    fun `GIVEN null google account WHEN logging in with Google THEN pass unknown error`() {
        testCoroutineScope.launch {
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.googleSignInLiveData.observeForever(googleSignInObserver)

            viewModel.handleSignInResult(null)

            verify(googleSignInObserver).onChanged(Either.left(Failure.UnknownError))
            verifyNoMoreInteractions(googleSignInObserver)
        }
    }

    @Test
    fun `GIVEN failed respon se WHEN logging in with Google THEN pass error`() {
        testCoroutineScope.launch {
            whenever(accountRepository.signWithGoogleAccount(idToken, user))
                .thenReturn(Either.left(Failure.UnavailableNetwork))
            viewModel = WelcomeViewModel(repository = accountRepository)
            viewModel.googleSignInLiveData.observeForever(googleSignInObserver)

            viewModel.handleSignInResult(googleAccount)

            verify(googleSignInObserver).onChanged(Either.left(Failure.UnavailableNetwork))
            verifyNoMoreInteractions(googleSignInObserver)
        }
    }
    //endregion
}