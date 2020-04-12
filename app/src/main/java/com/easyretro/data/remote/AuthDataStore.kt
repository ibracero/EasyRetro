package com.easyretro.data.remote

import arrow.core.Either
import com.easyretro.common.ConnectionManager
import com.easyretro.common.NetworkStatus
import com.easyretro.domain.Failure
import com.easyretro.domain.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthDataStore(private val connectionManager: ConnectionManager) {

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
                        Timber.d("signInWithCredential:success")
                        val user = firebaseAuth.currentUser
                        if (user != null) continuation.resume(Either.right(Unit))
                        else continuation.resume(Either.left(Failure.InvalidUserFailure))
                    } else continuation.parseExceptionAndResume(task.exception)
                }
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Either<Failure, UserStatus> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("signInWithEmail:success")
                        firebaseAuth.currentUser?.isEmailVerified?.let { verified ->
                            val userStatus =
                                if (verified) UserStatus.VERIFIED
                                else UserStatus.NON_VERIFIED
                            continuation.resume(Either.right(userStatus))
                        } ?: continuation.resume(Either.left(Failure.TokenExpiredFailure))
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
                    if (task.isSuccessful) sendEmailVerification(continuation)
                    else continuation.parseExceptionAndResume(task.exception)
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
                        Timber.d("resetPasswordEmail:success")
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

    suspend fun logOut() = suspendCoroutine<Unit> { firebaseAuth.signOut() }

    suspend fun reloadUser(isSessionStarted: Boolean): Either<Failure, UserStatus> {
        val currentUser = firebaseAuth.currentUser ?: return Either.left(Failure.UnknownError)
        return suspendCoroutine<Either<Failure, UserStatus>> { continuation ->
            currentUser.reload()
                .addOnCompleteListener { task ->
                    val sessionStarted =
                        if (task.isSuccessful) firebaseAuth.currentUser?.isEmailVerified
                        else isSessionStarted

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

    private fun <T> Continuation<Either<Failure, T>>.parseExceptionAndResume(exception: Exception?) {
        if (exception == null) return

        Timber.e(exception)
        val failure = Failure.parse(exception)
        resume(Either.left(failure))
    }
}