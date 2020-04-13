package com.easyretro.data.local

import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class LocalDataStore(private val retroDao: RetroDao) {

    fun getRetros() = retroDao.getUserRetros()

    fun saveRetros(retros: List<RetroDb>) {
        Timber.d("Updating retros ${retros.joinToString(",") { it.title }}")
        retroDao.insertRetros(retros)
    }

    fun saveStatements(statements: List<StatementDb>) {
        Timber.d("Updating statements ${statements.size}")
        retroDao.dropStatementsAndInsert(statements)
    }

    fun getPositiveStatements(retroUuid: String): Flow<List<StatementDb>> = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String): Flow<List<StatementDb>> = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String): Flow<List<StatementDb>> = retroDao.getActionPoints(retroUuid)

    fun getRetro(retroUuid: String): RetroDb? = retroDao.getRetro(retroUuid)

    fun updateRetroUsers(retroUuid: String, users: List<UserDb>) {
        retroDao.getRetro(retroUuid)?.let {
            retroDao.updateRetro(it.copy(users = users))
        }
    }

    fun clearAll() {
        retroDao.clearAll()
    }
}