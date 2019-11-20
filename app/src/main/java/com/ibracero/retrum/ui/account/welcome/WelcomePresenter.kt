package com.ibracero.retrum.ui.account.welcome

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.GoogleSignInCallback
import timber.log.Timber

class WelcomePresenter(
    private val repository: AccountRepository
) {

    fun isSessionOpen(): Boolean {
        return false
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>?, callback: GoogleSignInCallback) {
        try {
            val account = task?.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it, callback) }
        } catch (e: ApiException) {
            Timber.e(e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: GoogleSignInCallback) {
        repository.firebaseAuthWithGoogle(account, callback)
    }
}