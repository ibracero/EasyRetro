package com.easyretro.domain

import arrow.core.Either
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.User
import com.easyretro.domain.model.UserStatus

interface AccountRepository {

    suspend fun signWithGoogleAccount(idToken: String, user: User): Either<Failure, Unit>

    suspend fun signWithEmail(email: String, password: String): Either<Failure, UserStatus>

    suspend fun signUpWithEmail(email: String, password: String): Either<Failure, Unit>

    suspend fun logOut(): Unit

    suspend fun resetPassword(email: String): Either<Failure, Unit>

    suspend fun resendVerificationEmail(): Either<Failure, Unit>

    suspend fun getUserStatus(): Either<Failure, UserStatus>

}