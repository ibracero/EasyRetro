package com.easyretro.ui

import androidx.annotation.StringRes
import com.easyretro.R
import com.easyretro.domain.Failure

class FailureMessage {
    companion object {
        @StringRes
        fun parse(failure: Failure): Int {
            return when (failure) {
                Failure.UnknownError -> R.string.error_generic
                Failure.NetworkFailure -> R.string.error_network
                Failure.InvalidUserFailure -> R.string.error_invalid_user
                Failure.InvalidUserCredentialsFailure -> R.string.error_invalid_user_credentials
                Failure.TokenExpiredFailure -> R.string.error_token_expired
                Failure.UserCollisionFailure -> R.string.error_user_collision
                Failure.TooManyRequestsFailure -> R.string.error_too_many_requests
                Failure.CreateRetroError -> TODO()
                Failure.CreateStatementError -> TODO()
            }
        }
    }
}