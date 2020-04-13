package com.easyretro.domain

import arrow.core.Either
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType
import kotlinx.coroutines.flow.Flow

interface BoardRepository {

    suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>>

    suspend fun addStatement(retroUuid: String, description: String, type: StatementType): Either<Failure, Unit>

    suspend fun removeStatement(statement: Statement): Either<Failure, Unit>

    suspend fun startObservingStatements(retroUuid: String): Flow<Either<Failure, Unit>>

    suspend fun startObservingRetroUsers(retroUuid: String): Flow<Either<Failure, Unit>>
}