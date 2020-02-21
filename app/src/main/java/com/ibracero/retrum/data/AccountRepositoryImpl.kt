package com.ibracero.retrum.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.SignInCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore,
    private val localDataStore: LocalDataStore,
    dispatchers: CoroutineDispatcherProvider
) : AccountRepository {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun isSessionOpen(): Boolean = firebaseAuth.currentUser != null

    override fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: SignInCallback) {
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
                    callback.onSignedIn()
                } else {
                    callback.onError(task.exception ?: Exception("Couldn't sign in on Firebase"))
                }
            }
    }

    override fun loginUser(email: String, password: String, callback: SignInCallback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithEmail:success")
                    val user = firebaseAuth.currentUser
                    callback.onSignedIn()
                } else {
                    callback.onError(task.exception ?: Exception("Couldn't sign in on Firebase"))
                }
            }
    }

    override fun createUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification()
                } else {
                    Timber.d("signUpWithEmail:failed")
                }
            }
    }

    override fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("resetPasswordEmail:success")
                } else {
                    Timber.d("resetPasswordEmail:failed")
                }
            }
    }

    override fun logOut() {
        firebaseAuth.signOut()

        scope.launch {
            localDataStore.clearAll()
        }
    }

    private fun sendEmailVerification() {
        firebaseAuth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("sendEmailVerification:success")
                } else {
                    Timber.d("sendEmailVerification:failed")
                }
            }
    }
}
