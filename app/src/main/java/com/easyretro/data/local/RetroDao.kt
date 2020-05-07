package com.easyretro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RetroDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRetros(retros: List<RetroDb>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatements(statements: List<StatementDb>)

    @Query("SELECT * FROM $TABLE_RETRO ORDER BY timestamp ASC")
    fun getRetros(): List<RetroDb>

    @Query("SELECT * FROM $TABLE_RETRO WHERE uuid = :retroUuid")
    fun observeRetro(retroUuid: String): Flow<RetroDb>

    @Update
    fun updateRetro(retro: RetroDb)

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'POSITIVE' ORDER BY timestamp DESC")
    fun observePositiveStatements(retroUuid: String): Flow<List<StatementDb>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'NEGATIVE' ORDER BY timestamp DESC")
    fun observeNegativeStatements(retroUuid: String): Flow<List<StatementDb>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'ACTION_POINT' ORDER BY timestamp DESC")
    fun observeActionPoints(retroUuid: String): Flow<List<StatementDb>>

    @Query("DELETE FROM $TABLE_STATEMENT")
    fun deleteAllStatements()

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
        deleteAllRetros()
    }
}