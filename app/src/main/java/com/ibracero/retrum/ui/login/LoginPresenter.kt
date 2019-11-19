package com.ibracero.retrum.ui.login

import com.ibracero.retrum.domain.AccountRepository

class LoginPresenter(
    val repository: AccountRepository
) {

    fun createUser(email: String, password: String) {
        repository.createUser(email, password)
    }

    fun loginUser(email: String, password: String) {
        repository.loginUser(email, password)
    }

    fun loginGoogleUser() {
        repository.loginGoogleUser()
    }
}