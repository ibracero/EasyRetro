package com.ibracero.retrum.data.local

class LocalDataStore(private val retroDao: RetroDao) {

    fun save(retro: Retro) = retroDao.createOrUpdateRetro(retro)

    fun save(statement: Statement) = retroDao.insertStatements(listOf(statement))

    fun save(statements: List<Statement>) = retroDao.insertStatements(statements)

    fun getPositiveStatements(retroUuid: String) = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String) = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String) = retroDao.getActionPoints(retroUuid)
}