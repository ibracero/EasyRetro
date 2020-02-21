package com.ibracero.retrum.ui.account

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.SignInCallback

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    fun signIn(email: String, password: String) {
        repository.loginUser(email, password, object : SignInCallback {
            override fun onSignedIn() {

            }

            override fun onError(throwable: Throwable) {
            }
        })
    }

    fun signUp(email: String, password: String) {
        repository.createUser(email, password)
    }
}