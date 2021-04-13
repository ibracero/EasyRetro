package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionManager
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountRepositoryImplTest {

    private val userEmail = "email@email.com"
    private val userPassword = "password"
    private val userFirstName = "First name"
    private val userLastName = "Last name"
    private val userPhotoUrl = "photo.com/user1"
    private val domainUserOne = User(
        email = userEmail,
        firstName = userFirstName,
        lastName = userLastName,
        photoUrl = userPhotoUrl
    )
    private val userRemoteOne = UserRemote(
        email = userEmail,
        firstName = userFirstName,
        lastName = userLastName,
        photoUrl = userPhotoUrl
    )

    private val testCoroutineScope = TestCoroutineScope()
    private val dispatchers = mock<CoroutineDispatcherProvider> {
        on { main() }.then { testCoroutineScope.coroutineContext }
        on { io() }.then { testCoroutineScope.coroutineContext }
    }

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val sessionSharedPrefsManager = mock<SessionManager>()
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val repository: AccountRepository = AccountRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        sessionManager = sessionSharedPrefsManager,
        dispatchers = dispatchers
    )

    //region get user status
    @Test
    fun `GIVEN failed server response and session started WHEN getting user status THEN return VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = null
            whenever(authDataStore.isUserVerified()).thenReturn(Either.right(isUserVerifiedResponse))
            whenever(sessionSharedPrefsManager.isSessionStarted()).thenReturn(true)

            val actualUserStatus = repository.getUserStatus()

            verify(authDataStore).isUserVerified()
            assertEquals(Either.right(UserStatus.VERIFIED), actualUserStatus)
        }
    }

    @Test
    fun `GIVEN failed server response and session not started WHEN getting user status THEN return NON VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = null
            whenever(authDataStore.isUserVerified()).thenReturn(Either.right(isUserVerifiedResponse))
            whenever(sessionSharedPrefsManager.isSessionStarted()).thenReturn(false)

            val actualUserStatus = repository.getUserStatus()

            verify(authDataStore).isUserVerified()
            assertEquals(Either.right(UserStatus.NON_VERIFIED), actualUserStatus)
        }
    }

    @Test
    fun `GIVEN non verified user response and session started WHEN getting user status THEN return NON VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = false
            whenever(authDataStore.isUserVerified()).thenReturn(Either.right(isUserVerifiedResponse))

            val actualUserStatus = repository.getUserStatus()

            verify(authDataStore).isUserVerified()
            verifyZeroInteractions(sessionSharedPrefsManager)
            assertEquals(Either.right(UserStatus.NON_VERIFIED), actualUserStatus)
        }
    }

    @Test
    fun `GIVEN verified user response and session started WHEN getting user status THEN return VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = true
            whenever(authDataStore.isUserVerified()).thenReturn(Either.right(isUserVerifiedResponse))

            val actualUserStatus = repository.getUserStatus()

            verify(authDataStore).isUserVerified()
            verifyZeroInteractions(sessionSharedPrefsManager)
            assertEquals(Either.right(UserStatus.VERIFIED), actualUserStatus)
        }
    }
    //endregion

    //region sign in with google
    @Test
    fun `GIVEN success response from google WHEN signing with google THEN start session and return EitherRight`() {
        testCoroutineScope.launch {
            val tokenId = "googleToken"
            whenever(authDataStore.signInWithToken(token = tokenId)).thenReturn(Either.right(Unit))
            whenever(remoteDataStore.createUser(userRemoteOne)).thenReturn(Either.right(Unit))

            val result = repository.signWithGoogleAccount(idToken = tokenId, user = domainUserOne)

            verify(remoteDataStore).createUser(remoteUser = userRemoteOne)
            verify(sessionSharedPrefsManager).setSessionStarted()
            assertEquals(Either.right(Unit), result)
        }
    }

    @Test
    fun `GIVEN error response from google WHEN signing with google THEN return EitherLeft`() {
        testCoroutineScope.launch {
            val tokenId = "googleToken"
            whenever(authDataStore.signInWithToken(token = tokenId)).thenReturn(Either.left(Failure.UnknownError))

            val result = repository.signWithGoogleAccount(idToken = tokenId, user = domainUserOne)

            verifyZeroInteractions(remoteDataStore)
            verifyZeroInteractions(sessionSharedPrefsManager)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }

    @Test
    fun `GIVEN error response from the server WHEN signing with google THEN return EitherLeft`() {
        testCoroutineScope.launch {
            val tokenId = "googleToken"
            whenever(authDataStore.signInWithToken(token = tokenId)).thenReturn(Either.right(Unit))
            whenever(remoteDataStore.createUser(userRemoteOne)).thenReturn(Either.left(Failure.UnavailableNetwork))

            val result = repository.signWithGoogleAccount(idToken = tokenId, user = domainUserOne)

            verify(remoteDataStore).createUser(remoteUser = userRemoteOne)
            verifyZeroInteractions(sessionSharedPrefsManager)
            assertEquals(Either.left(Failure.UnavailableNetwork), result)
        }
    }
    //endregion

    //region sign in with email
    @Test
    fun `GIVEN VERIFIED user WHEN signing with email THEN start session and return VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = true
            whenever(
                authDataStore.signInWithEmailAndPassword(
                    email = userEmail,
                    password = userPassword
                )
            )
                .thenReturn(Either.right(isUserVerifiedResponse))

            val result = repository.signInWithEmail(email = userEmail, password = userPassword)

            verify(authDataStore).signInWithEmailAndPassword(
                email = userEmail,
                password = userPassword
            )
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.right(UserStatus.VERIFIED), result)
        }
    }

    @Test
    fun `GIVEN NON VERIFIED user WHEN signing with email THEN start session and return VERIFIED`() {
        testCoroutineScope.launch {
            val isUserVerifiedResponse = false
            whenever(
                authDataStore.signInWithEmailAndPassword(
                    email = userEmail,
                    password = userPassword
                )
            )
                .thenReturn(Either.right(isUserVerifiedResponse))

            val result = repository.signInWithEmail(email = userEmail, password = userPassword)

            verify(authDataStore).signInWithEmailAndPassword(
                email = userEmail,
                password = userPassword
            )
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.right(UserStatus.NON_VERIFIED), result)
        }
    }
    //endregion

    //region sign up with email
    @Test
    fun `GIVEN success server response WHEN signing up with email THEN return EitherRight`() {
        testCoroutineScope.launch {
            val remoteUser = UserRemote(email = userEmail)
            whenever(remoteDataStore.createUser(remoteUser)).thenReturn(Either.right(Unit))
            whenever(
                authDataStore.signUpWithEmailAndPassword(
                    email = userEmail,
                    password = userPassword
                )
            ).thenReturn(Either.right(Unit))

            val result = repository.signUpWithEmail(email = userEmail, password = userPassword)

            verify(authDataStore).signUpWithEmailAndPassword(
                email = userEmail,
                password = userPassword
            )
            verify(remoteDataStore).createUser(remoteUser)
            verifyNoMoreInteractions(authDataStore)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.right(Unit), result)
        }
    }


    @Test
    fun `GIVEN failed server response WHEN creating user in firebase with email THEN return EitherLeft`() {
        testCoroutineScope.launch {
            val remoteUser = UserRemote(email = userEmail)
            whenever(remoteDataStore.createUser(remoteUser)).thenReturn(Either.left(Failure.UnavailableNetwork))
            whenever(
                authDataStore.signUpWithEmailAndPassword(
                    email = userEmail,
                    password = userPassword
                )
            ).thenReturn(Either.right(Unit))

            val result = repository.signUpWithEmail(email = userEmail, password = userPassword)

            verify(authDataStore).signUpWithEmailAndPassword(
                email = userEmail,
                password = userPassword
            )
            verify(remoteDataStore).createUser(remoteUser)
            verifyNoMoreInteractions(authDataStore)
            verifyNoMoreInteractions(remoteDataStore)
            assertEquals(Either.left(Failure.UnavailableNetwork), result)
        }
    }

    @Test
    fun `GIVEN failed server response WHEN signing up with email THEN return EitherLeft`() {
        testCoroutineScope.launch {
            whenever(
                authDataStore.signUpWithEmailAndPassword(
                    email = userEmail,
                    password = userPassword
                )
            )
                .thenReturn(Either.left(Failure.UnknownError))

            val result = repository.signUpWithEmail(email = userEmail, password = userPassword)

            verify(authDataStore).signUpWithEmailAndPassword(
                email = userEmail,
                password = userPassword
            )
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }
    //endregion

    //region reset password
    @Test
    fun `GIVEN success server response WHEN resetting password THEN return EitherRight`() {
        testCoroutineScope.launch {
            whenever(authDataStore.resetPassword(email = userEmail)).thenReturn(Either.right(Unit))

            val result = repository.resetPassword(email = userEmail)

            verify(authDataStore).resetPassword(email = userEmail)
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.right(Unit), result)
        }
    }

    @Test
    fun `GIVEN failed server response WHEN resetting password THEN return EitherLeft`() {
        testCoroutineScope.launch {
            whenever(authDataStore.resetPassword(email = userEmail)).thenReturn(Either.left(Failure.UnknownError))

            val result = repository.resetPassword(email = userEmail)

            verify(authDataStore).resetPassword(email = userEmail)
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }
    //endregion

    //region resend verification email
    @Test
    fun `GIVEN success server response WHEN resending verification email THEN return EitherRight`() {
        testCoroutineScope.launch {
            whenever(authDataStore.resendVerificationEmail()).thenReturn(Either.right(Unit))

            val result = repository.resendVerificationEmail()

            verify(authDataStore).resendVerificationEmail()
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.right(Unit), result)
        }
    }

    @Test
    fun `GIVEN failed server response WHEN resending verification email THEN return EitherLeft`() {
        testCoroutineScope.launch {
            whenever(authDataStore.resendVerificationEmail()).thenReturn(Either.left(Failure.UnknownError))

            val result = repository.resendVerificationEmail()

            verify(authDataStore).resendVerificationEmail()
            verifyNoMoreInteractions(authDataStore)
            assertEquals(Either.left(Failure.UnknownError), result)
        }
    }
    //endregion

    //region resend verification email
    @Test
    fun `GIVEN regular flow WHEN logging out THEN finish local session, logout from firebase and clear database`() {
        testCoroutineScope.launch {

            val result = repository.logOut()

            inOrder(authDataStore, sessionSharedPrefsManager, localDataStore) {
                verify(localDataStore).clearAll()
                verify(sessionSharedPrefsManager).setSessionEnded()
                verify(authDataStore).logOut()
            }
            verifyNoMoreInteractions(sessionSharedPrefsManager)
            verifyNoMoreInteractions(authDataStore)
            verifyNoMoreInteractions(localDataStore)
            assertEquals(Unit, result)
        }
    }
    //endregion
}