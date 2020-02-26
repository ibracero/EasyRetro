package com.ibracero.retrum.domain

import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ibracero.retrum.data.remote.ServerError

interface AccountRepository {

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Either<ServerError, Unit>

    suspend fun loginUser(email: String, password: String): Either<ServerError, Unit>

    suspend fun createUser(email: String, password: String): Either<ServerError, Unit>

    suspend fun logOut(): Either<ServerError, Unit>

    suspend fun resetPassword(email: String): Either<ServerError, Unit>

    fun isSessionOpen(): Boolean
}