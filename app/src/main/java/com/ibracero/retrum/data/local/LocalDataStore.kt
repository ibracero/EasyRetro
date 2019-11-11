package com.ibracero.retrum.data.local

import timber.log.Timber

class LocalDataStore(private val retroDao: RetroDao) {

    fun getRetros() = retroDao.getUserRetros(/*"W2KCUn3Dz4Wy35CzQZmc"*/)

    fun save(retro: Retro) {
        Timber.d("saving retro $retro")
        retroDao.createOrUpdateRetro(retro)
    }

    fun save(statement: Statement) = retroDao.insertStatements(listOf(statement))

    fun save(statements: List<Statement>) = retroDao.insertStatements(statements)

    fun getPositiveStatements(retroUuid: String) = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String) = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String) = retroDao.getActionPoints(retroUuid)
}