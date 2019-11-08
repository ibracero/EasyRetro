package com.ibracero.retrum.domain

interface Repository {

    suspend fun openRetro()

    fun createUser()

    fun getStatements(statementType: StatementType)

    fun addStatement(statementType: StatementType, description: String)

    fun removeItem()
}