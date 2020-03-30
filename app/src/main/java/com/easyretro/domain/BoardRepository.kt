package com.easyretro.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.easyretro.data.local.Statement

interface BoardRepository {

    fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>>

    fun addStatement(retroUuid: String, description: String, statementType: StatementType): LiveData<Either<Failure, Unit>>

    fun removeStatement(statement: Statement)

    fun startObservingStatements(retroUuid: String)

    fun startObservingRetroUsers(retroUuid: String)

    fun stopObservingStatements()

    fun stopObservingRetroUsers()

    fun dispose()
}