package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionSharedPrefsManager
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
    private val sessionSharedPrefsManager: SessionSharedPrefsManager,
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

    override suspend fun signWithEmail(email: String, password: String): Either<Failure, UserStatus> =
        withContext(dispatchers.io()) {
            authDataStore.signInWithEmailAndPassword(email = email, password = password)
                .map { userStatus ->
                    sessionSharedPrefsManager.setSessionStarted()
                    userStatus
                }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.signUpWithEmailAndPassword(email = email, password = password)
        }

    override suspend fun resetPassword(email: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.resetPassword(email = email)
        }

    override suspend fun resendVerificationEmail(): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            authDataStore.resendVerificationEmail()
        }

    override suspend fun logOut(): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            try {
                sessionSharedPrefsManager.setSessionEnded()
                authDataStore.logOut()
                localDataStore.clearAll()
                Either.right(Unit)
            } catch (e: Exception) {
                Either.left(Failure.parse(e))
            }
        }

    private suspend fun reloadUser(): Either<Failure, UserStatus> =
        withContext(dispatchers.io()) {
            authDataStore.isUserVerified()
                .map { userVerified ->
                    when (userVerified ?: sessionSharedPrefsManager.isSessionStarted()) {
                        true -> UserStatus.VERIFIED
                        false -> UserStatus.NON_VERIFIED
                    }
                }
        }

    private suspend fun startUserSession(account: User): Either<Failure, Unit> =
        remoteDataStore.createUser(
            UserRemote(
                email = account.email,
                firstName = account.firstName,
                lastName = account.lastName,
                photoUrl = account.photoUrl
            )
        ).map { sessionSharedPrefsManager.setSessionStarted() }
}
