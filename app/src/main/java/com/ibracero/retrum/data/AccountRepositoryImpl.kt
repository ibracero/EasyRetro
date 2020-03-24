package com.ibracero.retrum.data

import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.UserStatus
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore,
    private val localDataStore: LocalDataStore,
    private val dispatchers: CoroutineDispatcherProvider
) : AccountRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun getUserStatus(): Either<Failure, UserStatus> =
        withContext(dispatchers.io) {
            reloadUser()
        }

    override suspend fun signWithGoogleAccount(account: GoogleSignInAccount): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithCredential:success")
                            val user = firebaseAuth.currentUser
                            user?.let {
                                remoteDataStore.createUser(
                                    email = account.email.orEmpty(),
                                    firstName = account.givenName.orEmpty(),
                                    lastName = account.familyName.orEmpty(),
                                    photoUrl = account.photoUrl?.toString().orEmpty()
                                )
                                continuation.resume(Either.right(Unit))
                            } ?: continuation.resume(Either.left(Failure.InvalidUserFailure))
                        } else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }

    override suspend fun signWithEmail(email: String, password: String): Either<Failure, UserStatus> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, UserStatus>> { continuation ->
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithEmail:success")
                            firebaseAuth.currentUser?.isEmailVerified?.let { verified ->
                                val userStatus = if (verified) UserStatus.VERIFIED
                                else UserStatus.NON_VERIFIED
                                continuation.resume(Either.right(userStatus))
                            } ?: continuation.resume(Either.left(Failure.TokenExpiredFailure))
                        } else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) sendEmailVerification(continuation)
                        else continuation.parseExceptionAndResume(task.exception)
                    }
            }
        }

    override suspend fun resetPassword(email: String): Either<Failure, Unit> =
        withContext(dispatchers.io) {
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

    override suspend fun resendVerificationEmail(): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { sendEmailVerification(it) }
        }

    override suspend fun logOut(): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            suspendCoroutine<Either<Failure, Unit>> { continuation ->
                try {
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
        return suspendCoroutine { continuation ->
            currentUser.reload()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val status = when (firebaseAuth.currentUser?.isEmailVerified) {
                            true -> Either.right(UserStatus.VERIFIED)
                            false -> Either.right(UserStatus.NON_VERIFIED)
                            null -> Either.left(Failure.InvalidUserFailure)
                        }
                        continuation.resume(status)
                    } else continuation.parseExceptionAndResume(task.exception)
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

    private fun <T> Continuation<Either<Failure, T>>.parseExceptionAndResume(exception: Exception?) {
        if (exception == null) return

        Timber.e(exception)
        val failure = Failure.parse(exception)
        resume(Either.left(failure))
    }
}
