package com.ibracero.retrum.domain

interface AccountRepository {

    fun loginGoogleUser()

    fun loginUser(email: String, password: String)

    fun createUser(email: String, password: String)

    fun logOut()
}