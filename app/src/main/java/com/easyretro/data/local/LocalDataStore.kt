package com.easyretro.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun observePositiveStatements(retroUuid: String): Flow<List<StatementDb>> =
        retroDao.getPositiveStatements(retroUuid)

    fun observeNegativeStatements(retroUuid: String): Flow<List<StatementDb>> =
        retroDao.getNegativeStatements(retroUuid)

    fun observeActionPoints(retroUuid: String): Flow<List<StatementDb>> = retroDao.getActionPoints(retroUuid)

    fun getRetro(retroUuid: String): RetroDb? = retroDao.getRetro(retroUuid)

    fun updateRetro(retro: RetroDb) {
        Timber.d("Updating retro $retro")
        retroDao.updateRetro(retro)
    }

    fun clearAll() {
        retroDao.clearAll()
    }
}