package com.easyretro.domain

interface SignInCallback {

    fun onSignedIn()

    fun onError(throwable: Throwable)
}