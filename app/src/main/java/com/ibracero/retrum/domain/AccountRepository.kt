package com.ibracero.retrum.domain

import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AccountRepository {

    suspend fun signWithGoogleAccount(account: GoogleSignInAccount): Either<Failure, Unit>

    suspend fun signWithEmail(email: String, password: String): Either<Failure, UserStatus>

    suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit>

    suspend fun logOut(): Either<Failure, Unit>

    suspend fun resetPassword(email: String): Either<Failure, Unit>

    suspend fun resendVerificationEmail(): Either<Failure, Unit>

    suspend fun getUserStatus(): Either<Failure, UserStatus>

}