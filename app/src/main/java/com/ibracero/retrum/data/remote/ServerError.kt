package com.ibracero.retrum.data.remote

sealed class ServerError {
    object CreateRetroError : ServerError()
    object CreateStatementError : ServerError()
    object GetRetrosError : ServerError()
    object GetStatementsError : ServerError()
    object RemoveStatementError : ServerError()
}