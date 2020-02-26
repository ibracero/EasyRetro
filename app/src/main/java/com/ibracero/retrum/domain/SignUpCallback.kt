package com.ibracero.retrum.domain

interface SignUpCallback {

    fun onEmailVerificationSent()

    fun onError(throwable: Throwable)
}