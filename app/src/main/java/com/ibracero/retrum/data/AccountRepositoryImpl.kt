package com.ibracero.retrum.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.SignInCallback
import timber.log.Timber

class AccountRepositoryImpl(
    private val remoteDataStore: RemoteDataStore
) : AccountRepository {

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun logOut() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
