package com.ibracero.retrum.domain

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AccountRepository {

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: GoogleSignInCallback)

    fun loginUser(email: String, password: String)

    fun createUser(email: String, password: String)

    fun logOut()
}