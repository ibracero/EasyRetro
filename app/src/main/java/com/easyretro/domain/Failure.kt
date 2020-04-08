package com.easyretro.domain

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

sealed class Failure constructor(message: String? = null) {
    companion object {
        fun parse(exception: Exception): Failure {
            return when (exception) {
                is FirebaseNetworkException -> NetworkFailure
                is FirebaseAuthInvalidUserException -> InvalidUserFailure
                is FirebaseAuthInvalidCredentialsException -> InvalidUserCredentialsFailure
                is FirebaseAuthRecentLoginRequiredException -> TokenExpiredFailure
                is FirebaseAuthUserCollisionException -> UserCollisionFailure
                is FirebaseTooManyRequestsException -> TooManyRequestsFailure
                else -> UnknownError
            }
        }
    }

    object UnknownError : Failure()
    object CreateRetroError : Failure()
    object CreateStatementError : Failure()
    object RemoveStatementError : Failure()

    object NetworkFailure : Failure() // Unreachable host
    object InvalidUserFailure : Failure() // User not found / User disabled / token expired  / invalid token
    object InvalidUserCredentialsFailure :
        Failure() // Invalid credentials when trying to identify / authenticate a user

    object TokenExpiredFailure : Failure() // Credentials no longer valid. Login again
    object UserCollisionFailure : Failure() // User is already in use
    object TooManyRequestsFailure : Failure() // Too many requests done in a brief period of time
}