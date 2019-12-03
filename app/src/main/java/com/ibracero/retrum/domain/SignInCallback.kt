package com.ibracero.retrum.domain

interface SignInCallback {

    fun onSignedIn()

    fun onError(throwable: Throwable)
}