package com.easyretro.data.local

class LocalDataStore(private val retroDao: RetroDao) {

    fun getRetros() = retroDao.getUserRetros()

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

    fun getRetro(retroUuid: String) = retroDao.getRetro(retroUuid)

    fun updateRetroUsers(retroUuid: String, users: List<User>) {
        val retro = retroDao.getRetro(retroUuid)
        retroDao.updateRetro(retro.copy(users = users))
    }

    fun clearAll() {
        retroDao.clearAll()
    }
}