package com.ibracero.retrum.data

import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.data.remote.ServerError
import com.ibracero.retrum.domain.AccountRepository
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore,
    private val localDataStore: LocalDataStore
) : AccountRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun isSessionOpen(): Boolean = firebaseAuth.currentUser?.isEmailVerified == true

    override suspend fun getUserStatus(): AccountRepository.UserStatus {
        val currentUser = firebaseAuth.currentUser ?: return AccountRepository.UserStatus.UNKNOWN
        return suspendCoroutine { continuation ->
            currentUser.reload()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val status = when (firebaseAuth.currentUser?.isEmailVerified) {
                            true -> AccountRepository.UserStatus.VERIFIED
                            false -> AccountRepository.UserStatus.NON_VERIFIED
                            null -> AccountRepository.UserStatus.UNKNOWN
                        }
                        continuation.resume(status)
                    } else continuation.resume(AccountRepository.UserStatus.UNKNOWN)
                }
        }
    }

    override suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Either<ServerError, Unit> {
        return suspendCoroutine { continuation ->
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
                        }
                        continuation.resume(Either.right(Unit))
                    } else {
                        Timber.e(task.exception, "signInWithCredential:failed")
                        continuation.resume(Either.left(ServerError.GoogleSignInError))
                    }
                }
        }
    }

    override suspend fun loginUser(email: String, password: String): Either<ServerError, AccountRepository.UserStatus> {
        return suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("signInWithEmail:success")
                        val isVerified = firebaseAuth.currentUser?.isEmailVerified ?: false
                        val userStatus = if (isVerified) AccountRepository.UserStatus.VERIFIED
                        else AccountRepository.UserStatus.NON_VERIFIED
                        continuation.resume(Either.right(userStatus))
                    } else {
                        Timber.e(task.exception, "signInWithEmail:failed")
                        continuation.resume(Either.left(ServerError.GoogleSignInError))
                    }
                }
        }
    }

    override suspend fun createUser(email: String, password: String): Either<ServerError, Unit> {
        return suspendCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) sendEmailVerification(continuation)
                    else {
                        Timber.e(task.exception, "signUpWithEmail:failed")
                        continuation.resume(Either.left(ServerError.SignUpError))
                    }
                }
        }
    }

    override suspend fun resetPassword(email: String): Either<ServerError, Unit> {
        return suspendCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("resetPasswordEmail:success")
                        continuation.resume(Either.right(Unit))
                    } else {
                        Timber.d("resetPasswordEmail:failed")
                        continuation.resume(Either.left(ServerError.ResetPasswordError))
                    }
                }
        }
    }

    override suspend fun resendVerificationEmail(): Either<ServerError, Unit> {
        return suspendCoroutine { sendEmailVerification(it) }
    }

    private fun sendEmailVerification(continuation: Continuation<Either<ServerError, Unit>>) {
        firebaseAuth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("sendEmailVerification:success")
                    continuation.resume(Either.right(Unit))
                } else {
                    Timber.e(task.exception, "sendEmailVerification:failed")
                    continuation.resume(Either.left(ServerError.SendVerificationEmailError))
                }
            }
    }

    override suspend fun logOut(): Either<ServerError, Unit> {
        return suspendCoroutine { continuation ->
            try {
                firebaseAuth.signOut()
                localDataStore.clearAll()
                continuation.resume(Either.right(Unit))
            } catch (e: Exception) {
                continuation.resume(Either.left(ServerError.LogoutError))
            }
        }
    }
}
