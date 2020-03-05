package com.ibracero.retrum.domain

import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ibracero.retrum.data.AccountRepositoryImpl
import com.ibracero.retrum.data.remote.ServerError

interface AccountRepository {

    enum class UserStatus {
        VERIFIED, NON_VERIFIED, UNKNOWN
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Either<ServerError, Unit>

    suspend fun loginUser(email: String, password: String): Either<ServerError, UserStatus>

    suspend fun createUser(email: String, password: String): Either<ServerError, Unit>

    suspend fun logOut(): Either<ServerError, Unit>

    suspend fun resetPassword(email: String): Either<ServerError, Unit>

    suspend fun resendVerificationEmail(): Either<ServerError, Unit>

    suspend fun getUserStatus(): UserStatus

    fun isSessionOpen(): Boolean
}