package com.easyretro.domain

interface SignUpCallback {

    fun onEmailVerificationSent()

    fun onError(throwable: Throwable)
}