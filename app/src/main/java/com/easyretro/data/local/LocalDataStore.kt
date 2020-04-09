package com.easyretro.data.local

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

    fun getPositiveStatements(retroUuid: String) = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String) = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String) = retroDao.getActionPoints(retroUuid)

    fun getRetro(retroUuid: String) = retroDao.getRetro(retroUuid)

    fun updateRetroUsers(retroUuid: String, users: List<User>) {
        val retro = retroDao.getRetro(retroUuid)
        retroDao.updateRetro(retro.copy(users = users))
    }

    fun clearAll() {
        retroDao.clearAll()
    }
}