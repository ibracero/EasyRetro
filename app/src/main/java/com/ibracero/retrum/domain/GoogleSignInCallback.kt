package com.ibracero.retrum.domain

interface GoogleSignInCallback {

    fun onSignedIn()

    fun onError(throwable: Throwable)
}