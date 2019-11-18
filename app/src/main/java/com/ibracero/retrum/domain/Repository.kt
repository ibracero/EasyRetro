package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import arrow.core.Either
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.remote.ServerError

interface Repository {

    fun createRetro(title: String): LiveData<Either<ServerError, Retro>>

    fun getRetros(): LiveData<List<Retro>>

    fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>>

    fun addStatement(retroUuid: String, description: String, statementType: StatementType)

    fun removeStatement(statement: Statement)

    fun dispose()

    fun startObservingStatements(retroUuid: String)

    fun startObservingUserRetros()

    fun stopObservingStatements()

    fun stopObservingUserRetros()
}