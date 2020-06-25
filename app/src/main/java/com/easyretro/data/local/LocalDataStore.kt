package com.easyretro.data.local

import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class LocalDataStore @Inject constructor(
    private val retroDao: RetroDao
) {

    fun getRetros(): List<RetroDb> = retroDao.getRetros()

    fun saveRetros(retros: List<RetroDb>) {
        Timber.d("Updating local retros ${retros.joinToString(",") { it.title }}")
        retroDao.insertRetros(retros)
    }

    fun saveStatements(statements: List<StatementDb>) {
        Timber.d("Updating local statements ${statements.size}")
        retroDao.dropStatementsAndInsert(statements)
    }

    fun observePositiveStatements(retroUuid: String): Flow<List<StatementDb>> =
        retroDao.observePositiveStatements(retroUuid)

    fun observeNegativeStatements(retroUuid: String): Flow<List<StatementDb>> =
        retroDao.observeNegativeStatements(retroUuid)

    fun observeActionPoints(retroUuid: String): Flow<List<StatementDb>> = retroDao.observeActionPoints(retroUuid)

    fun observeRetro(retroUuid: String): Flow<RetroDb?> = retroDao.observeRetro(retroUuid)

    fun updateRetro(retro: RetroDb) {
        Timber.d("Updating local retro $retro")
        retroDao.updateRetro(retro)
    }

    fun clearAll() {
        retroDao.clearAll()
        Timber.d("Database cleared")
    }
}