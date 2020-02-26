package com.ibracero.retrum.data.remote

sealed class ServerError {
    object GoogleSignInError: ServerError()
    object SignUpError: ServerError()
    object SendVerificationEmailError: ServerError()
    object ResetPasswordError: ServerError()
    object LogoutError: ServerError()
    object CreateRetroError : ServerError()
    object CreateStatementError : ServerError()
    object GetRetrosError : ServerError()
    object GetStatementsError : ServerError()
    object RemoveStatementError : ServerError()
}