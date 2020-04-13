package com.easyretro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RetroDao {

    @Query("SELECT * FROM $TABLE_RETRO ORDER BY timestamp ASC")
    fun getUserRetros(): List<RetroDb>

    @Query("SELECT * FROM $TABLE_RETRO WHERE uuid = :retroUuid")
    fun getRetro(retroUuid: String): RetroDb?

    @Update
    fun updateRetro(retro: RetroDb)

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'POSITIVE' ORDER BY timestamp DESC")
    fun getPositiveStatements(retroUuid: String): Flow<List<StatementDb>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'NEGATIVE' ORDER BY timestamp DESC")
    fun getNegativeStatements(retroUuid: String): Flow<List<StatementDb>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'ACTION_POINT' ORDER BY timestamp DESC")
    fun getActionPoints(retroUuid: String): Flow<List<StatementDb>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRetros(retros: List<RetroDb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatements(statements: List<StatementDb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserDb)

    @Query("DELETE FROM $TABLE_STATEMENT")
    fun deleteAllStatements()

    @Query("DELETE FROM $TABLE_USER")
    fun deleteUserInfo()

    @Query("DELETE FROM $TABLE_RETRO")
    fun deleteAllRetros()

    @Transaction
    fun dropStatementsAndInsert(statements: List<StatementDb>) {
        deleteAllStatements()
        insertStatements(statements)
    }

    @Transaction
    fun clearAll() {
        deleteAllStatements()
        deleteUserInfo()
        deleteAllRetros()
    }
}