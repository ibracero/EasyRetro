package com.ibracero.retrum.ui.account.welcome

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.SignInCallback
import timber.log.Timber

class WelcomePresenter(
    private val repository: AccountRepository
) {

    fun isSessionOpen(): Boolean {
        return repository.isSessionOpen()
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>?, callback: SignInCallback) {
        try {
            val account = task?.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it, callback) }
        } catch (e: ApiException) {
            Timber.e(e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: SignInCallback) {
        repository.firebaseAuthWithGoogle(account, callback)
    }
}