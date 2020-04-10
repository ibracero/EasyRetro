package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.ConnectionManager
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.common.NetworkStatus
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.SessionSharedPrefsManager
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.Failure
import com.easyretro.domain.UserStatus
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore,
    private val localDataStore: LocalDataStore,
    private val sessionSharedPrefsManager: SessionSharedPrefsManager,
    private val dispatchers: CoroutineDispatcherProvider,
    private val connectionManager: ConnectionManager
) : AccountRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun getUserStatus(): Either<Failure, UserStatus> =
        withContext(dispatchers.io) {
            reloadUser()
        }

    override suspend fun signWithGoogleAccount(account: GoogleSignInAccount): Either<Failure, Unit> {
        return if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            Either.left(Failure.UnavailableNetwork)
        else withContext(dispatchers.io) {
            val signInEither = suspendCoroutine<Either<Failure, Unit>> { continuation ->
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithCredential:success")
                            val user = firebaseAuth.currentUser
                            if (user != null) continuation.resume(Either.right(Unit))
                            else continuation.resume(Either.left(Failure.InvalidUserFailure))
                        } else continuation.parseExceptionAndResume(task.exception)
                    }
            }

            if (signInEither.isLeft()) signInEither
            else startUserSession(account)
        }
    }

    override suspend fun signWithEmail(email: String, password: String): Either<Failure, UserStatus> {
        return if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            Either.left(Failure.UnavailableNetwork)
        else withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, UserStatus>> { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithEmail:success")
                            firebaseAuth.currentUser?.isEmailVerified?.let { verified ->
                                val userStatus =
                                    if (verified) {
                                        sessionSharedPrefsManager.setSessionStarted()
                                        UserStatus.VERIFIED
                                    } else UserStatus.NON_VERIFIED
                                continuation.resume(Either.right(userStatus))
                            } ?: continuation.resume(Either.left(Failure.TokenExpiredFailure))
                        } else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit> {
        return if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            Either.left(Failure.UnavailableNetwork)
        else withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) sendEmailVerification(continuation)
                        else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }
    }

    override suspend fun resetPassword(email: String): Either<Failure, Unit> {
        return if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            Either.left(Failure.UnavailableNetwork)
        else withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("resetPasswordEmail:success")
                            continuation.resume(Either.right(Unit))
                        } else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }
    }

    override suspend fun resendVerificationEmail(): Either<Failure, Unit> {
        return if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            Either.left(Failure.UnavailableNetwork)
        else withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { sendEmailVerification(it) }
        }
    }

    override suspend fun logOut(): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                try {
                    sessionSharedPrefsManager.setSessionEnded()
                    firebaseAuth.signOut()
                    localDataStore.clearAll()
                    continuation.resume(Either.right(Unit))
                } catch (e: Exception) {
                    continuation.parseExceptionAndResume(e)
                }
            }
        }

    private suspend fun reloadUser(): Either<Failure, UserStatus> {
        val currentUser = firebaseAuth.currentUser ?: return Either.left(Failure.UnknownError)
        return suspendCoroutine<Either<Failure, UserStatus>> { continuation ->
            currentUser.reload()
                .addOnCompleteListener { task ->
                    val sessionStarted =
                        if (task.isSuccessful) firebaseAuth.currentUser?.isEmailVerified
                        else sessionSharedPrefsManager.isSessionStarted()

                    val status = when (sessionStarted) {
                        true -> Either.right(UserStatus.VERIFIED)
                        false -> Either.right(UserStatus.NON_VERIFIED)
                        null -> Either.left(Failure.InvalidUserFailure)
                    }
                    continuation.resume(status)
                }
        }
    }

    private fun sendEmailVerification(continuation: Continuation<Either<Failure, Unit>>) {
        firebaseAuth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("sendEmailVerification:success")
                    continuation.resume(Either.right(Unit))
                } else continuation.parseExceptionAndResume(task.exception)
            }
    }

    private suspend fun startUserSession(account: GoogleSignInAccount): Either<Failure, Unit> =
        remoteDataStore.createUser(
            email = account.email.orEmpty(),
            firstName = account.givenName.orEmpty(),
            lastName = account.familyName.orEmpty(),
            photoUrl = account.photoUrl?.toString().orEmpty()
        ).map { sessionSharedPrefsManager.setSessionStarted() }

    private fun <T> Continuation<Either<Failure, T>>.parseExceptionAndResume(exception: Exception?) {
        if (exception == null) return

        Timber.e(exception)
        val failure = Failure.parse(exception)
        resume(Either.left(failure))
    }
}
