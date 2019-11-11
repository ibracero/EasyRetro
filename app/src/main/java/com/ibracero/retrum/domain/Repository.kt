package com.ibracero.retrum.domain

import androidx.lifecycle.LiveData
import com.ibracero.retrum.data.local.Statement

interface Repository {

    fun loadRetro()

    fun createUser()

    fun getStatements(statementType: StatementType): LiveData<List<Statement>>

    fun addStatement(statement: Statement? = null)

    fun removeItem()
}