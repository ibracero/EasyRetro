package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.remote.ServerError

interface BoardRepository {

    fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>>

    fun addStatement(retroUuid: String, description: String, statementType: StatementType): LiveData<Either<ServerError, Unit>>

    fun removeStatement(statement: Statement)

    fun startObservingStatements(retroUuid: String)

    fun startObservingRetroUsers(retroUuid: String)

    fun stopObservingStatements()

    fun stopObservingRetroUsers()

    fun dispose()
}