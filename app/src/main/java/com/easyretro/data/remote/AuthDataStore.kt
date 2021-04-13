package com.easyretro.data.remote

import arrow.core.Either
import com.easyretro.common.ConnectionManager
import com.easyretro.common.NetworkStatus
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthDataStore @Inject constructor(
    private val connectionManager: ConnectionManager
) {

    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    fun getCurrentUserEmail() = firebaseAuth.currentUser?.email.orEmpty()

    suspend fun signInWithToken(token: String?): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        val credential = GoogleAuthProvider.getCredential(token, null)
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("User signed in with token (Google Account)")
                        val user = firebaseAuth.currentUser
                        if (user != null) continuation.resume(Either.right(Unit))
                        else continuation.resume(Either.left(Failure.InvalidUserFailure))
                    } else continuation.parseExceptionAndResume(task.exception)
                }
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Either<Failure, Boolean> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("User signed in with email")
                        val user = firebaseAuth.currentUser
                        if (user != null) continuation.resume(Either.right(user.isEmailVerified))
                        else continuation.resume(Either.left(Failure.InvalidUserFailure))
                    } else continuation.parseExceptionAndResume(task.exception)
                }
        }
    }

    suspend fun signUpWithEmailAndPassword(email: String, password: String): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("User signed up with email and password")
                        sendEmailVerification(continuation)
                    } else continuation.parseExceptionAndResume(task.exception)
                }
        }
    }

    suspend fun resetPassword(email: String): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Password email reset sent")
                        continuation.resume(Either.right(Unit))
                    } else continuation.parseExceptionAndResume(task.exception)
                }
        }
    }

    suspend fun resendVerificationEmail(): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { sendEmailVerification(it) }
    }

    suspend fun isUserVerified(): Either<Failure, Boolean?> {
        val currentUser = firebaseAuth.currentUser ?: return Either.left(Failure.InvalidUserFailure)
        return suspendCoroutine { continuation ->
            currentUser.reload()
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) continuation.resume(Either.right(null))
                    else continuation.resume(Either.right(firebaseAuth.currentUser?.isEmailVerified))
                }
        }
    }

    suspend fun logOut() = suspendCoroutine<Unit> {
        firebaseAuth.signOut()
        Timber.d("User signed out")
    }

    private fun sendEmailVerification(continuation: Continuation<Either<Failure, Unit>>) {
        firebaseAuth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("User verification email sent")
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