package com.easyretro.data

import arrow.core.Either
import com.easyretro.CoroutineTestRule
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionSharedPrefsManager
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccountRepositoryImplTest {

    private val userEmail = "email@email.com"
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

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val localDataStore = mock<LocalDataStore>()
    private val remoteDataStore = mock<RemoteDataStore>()
    private val sessionSharedPrefsManager = mock<SessionSharedPrefsManager>()
    private val authDataStore = mock<AuthDataStore> {
        on { getCurrentUserEmail() }.thenReturn(userEmail)
    }

    private val repository: AccountRepository = AccountRepositoryImpl(
        localDataStore = localDataStore,
        remoteDataStore = remoteDataStore,
        authDataStore = authDataStore,
        sessionSharedPrefsManager = sessionSharedPrefsManager,
        dispatchers = coroutinesTestRule.testDispatcherProvider
    )

    //region get user status
    @Test
    fun `GIVEN failed server response and session started WHEN getting user status THEN return local session`() {
        runBlocking {
            val isUserVerifiedResponse = null
            whenever(authDataStore.isUserVerified()).thenReturn(Either.right(isUserVerifiedResponse))
            whenever(sessionSharedPrefsManager.isSessionStarted()).thenReturn(true)

            val actualUserStatus = repository.getUserStatus()

            verify(authDataStore).isUserVerified()
            assertEquals(Either.right(UserStatus.VERIFIED), actualUserStatus)
        }
    }

    @Test
    fun `GIVEN non verified user response and session started WHEN getting user status THEN return NON VERIFIED`() {
        runBlocking {
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
        runBlocking {
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
        runBlocking {
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
        runBlocking {
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
        runBlocking {
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
}