package com.ibracero.retrum.domain

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AccountRepository {

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: SignInCallback)

    fun loginUser(email: String, password: String, callback: SignInCallback)

    fun createUser(email: String, password: String)

    fun logOut()

    fun isSessionOpen(): Boolean

    fun resetPassword(email: String)
}