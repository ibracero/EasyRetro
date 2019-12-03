package com.ibracero.retrum.ui.account.login

import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.SignInCallback

class LoginPresenter(private val repository: AccountRepository) {

    fun signIn(email: String, password: String) {
        repository.loginUser(email, password, object : SignInCallback {
            override fun onSignedIn() {

            }

            override fun onError(throwable: Throwable) {
            }
        })
    }
}