package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement

interface Repository {

    fun createRetro(title: String): LiveData<Retro>

    fun getRetros(): LiveData<List<Retro>>

    fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>>

    fun addStatement(statement: Statement? = null)

    fun removeItem()

    fun dispose()
}