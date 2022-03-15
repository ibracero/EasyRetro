package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.SessionManager
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore,
    private val localDataStore: LocalDataStore,
    private val authDataStore: AuthDataStore,
    private val sessionManager: SessionManager,
    private val dispatchers: CoroutineDispatcherProvider
) : AccountRepository {

    override suspend fun getUserStatus(): Either<Failure, UserStatus> =
        withContext(dispatchers.io()) {
            reloadUser()
        }

    override suspend fun signWithGoogleAccount(idToken: String, user: User): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.signInWithToken(token = idToken)
            val signInEither = authDataStore.signInWithToken(token = idToken)
            if (signInEither.isLeft()) signInEither
            else startUserSession(user)
        }

    override suspend fun signInWithEmail(email: String, password: String): Either<Failure, UserStatus> =
        withContext(dispatchers.io()) {
            authDataStore.signInWithEmailAndPassword(email = email, password = password)
                .map { isUserVerified ->
                    if (isUserVerified) {
                        sessionManager.setSessionStarted()
                        UserStatus.VERIFIED
                    } else UserStatus.NON_VERIFIED
                }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            val signUpEither = authDataStore.signUpWithEmailAndPassword(email = email, password = password)
            if (signUpEither.isLeft()) signUpEither
            else remoteDataStore.createUser(UserRemote(email = email))
        }

    override suspend fun resetPassword(email: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.resetPassword(email = email)
        }

    override suspend fun resendVerificationEmail(): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.resendVerificationEmail()
        }

    override suspend fun logOut(): Unit =
        withContext(dispatchers.io()) {
            localDataStore.clearAll()
            sessionManager.setSessionEnded()
            authDataStore.logOut()
        }

    private suspend fun reloadUser(): Either<Failure, UserStatus> =
        withContext(dispatchers.io()) {
            authDataStore.isUserVerified()
                .map { userVerified ->
                    if (userVerified ?: sessionManager.isSessionStarted()) UserStatus.VERIFIED
                    else UserStatus.NON_VERIFIED
                }
        }

    private suspend fun startUserSession(account: User): Either<Failure, Unit> {
        val remoteUser = UserRemote(
            email = account.email,
            firstName = account.firstName,
            lastName = account.lastName,
            photoUrl = account.photoUrl
        )
        return remoteDataStore.createUser(remoteUser).map { sessionManager.setSessionStarted() }
    }
}
