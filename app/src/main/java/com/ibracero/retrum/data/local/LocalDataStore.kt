package com.ibracero.retrum.data.local

class LocalDataStore(private val retroDao: RetroDao) {

    fun getRetros() = retroDao.getUserRetros(/*"W2KCUn3Dz4Wy35CzQZmc"*/)

    fun saveRetros(retros: List<Retro>) {
        retroDao.insertRetros(retros)
    }

    fun saveStatements(statements: List<Statement>) {
        retroDao.dropStatementsAndInsert(statements)
    }

    fun saveUser(user: User) {
        retroDao.insertUser(user)
    }

    fun getPositiveStatements(retroUuid: String) = retroDao.getPositiveStatements(retroUuid)

    fun getNegativeStatements(retroUuid: String) = retroDao.getNegativeStatements(retroUuid)

    fun getActionPoints(retroUuid: String) = retroDao.getActionPoints(retroUuid)

    fun getRetroInfo(retroUuid: String) = retroDao.gerRetroInfo(retroUuid)
}