package com.easyretro.data.local

import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class LocalDataStore(private val retroDao: RetroDao) {

    fun getRetros() = retroDao.getUserRetros()

    fun saveRetros(retros: List<Retro>) {
        Timber.d("Updating retros ${retros.joinToString(",") { it.title }}")
        retroDao.insertRetros(retros)
    }

    fun saveStatements(statements: List<Statement>) {
        Timber.d("Updating statements ${statements.size}")
        retroDao.dropStatementsAndInsert(statements)
    }

    fun getPositiveStatements(retroUuid: String): Flow<List<Statement>> = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String): Flow<List<Statement>> = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String): Flow<List<Statement>> = retroDao.getActionPoints(retroUuid)

    fun getRetro(retroUuid: String): Retro? = retroDao.getRetro(retroUuid)

    fun updateRetroUsers(retroUuid: String, users: List<User>) {
        retroDao.getRetro(retroUuid)?.let {
            retroDao.updateRetro(it.copy(users = users))
        }
    }

    fun clearAll() {
        retroDao.clearAll()
    }
}