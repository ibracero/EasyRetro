package com.easyretro.domain

import arrow.core.Either
import com.easyretro.data.local.Statement
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>>

    suspend fun addStatement(
        retroUuid: String,
        description: String,
        statementType: StatementType
    ): Either<Failure, Unit>

    suspend fun removeStatement(statement: Statement): Either<Failure, Unit>

    fun startObservingStatements(retroUuid: String)

    fun startObservingRetroUsers(retroUuid: String)

    fun stopObservingStatements()

    fun stopObservingRetroUsers()

    fun dispose()
}