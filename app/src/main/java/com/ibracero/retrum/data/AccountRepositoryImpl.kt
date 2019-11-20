package com.ibracero.retrum.data

import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ibracero.retrum.R
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.GoogleSignInCallback
import timber.log.Timber
import java.lang.Exception

class AccountRepositoryImpl : AccountRepository {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: GoogleSignInCallback) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    callback.onSignedIn()
                } else {
                    callback.onError(task.exception ?: Exception("Couldn't sign in on Firebase"))
                }
            }
    }

    override fun loginUser(email: String, password: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createUser(email: String, password: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun logOut() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}