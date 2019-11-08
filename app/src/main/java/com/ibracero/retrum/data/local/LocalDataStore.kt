package com.ibracero.retrum.data.local

class LocalDataStore(private val retroDao: RetroDao) {

    fun createOrUpdateRetro(retro: Retro) = retroDao.createOrUpdateRetro(retro)

    fun getPositiveStatements(retroUuid: String) = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String) = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String) = retroDao.getActionPoints(retroUuid)
}