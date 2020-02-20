package com.ibracero.retrum.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ibracero.retrum.domain.StatementType

@Dao
interface RetroDao {

    @Query("SELECT * FROM $TABLE_RETRO ORDER BY timestamp ASC")
    fun getUserRetros(): LiveData<List<Retro>>

    @Query("SELECT * FROM $TABLE_RETRO WHERE uuid = :retroUuid")
    fun getRetroInfo(retroUuid: String): LiveData<Retro>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'POSITIVE' ORDER BY timestamp DESC")
    fun getPositiveStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'NEGATIVE' ORDER BY timestamp DESC")
    fun getNegativeStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'ACTION_POINT' ORDER BY timestamp DESC")
    fun getActionPoints(retroUuid: String): LiveData<List<Statement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRetros(retros: List<Retro>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatements(statements: List<Statement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM $TABLE_STATEMENT")
    fun deleteAllStatements()

    @Query("DELETE FROM $TABLE_USER")
    fun deleteUserInfo()

    @Query("DELETE FROM $TABLE_RETRO")
    fun deleteAllRetros()

    @Query("SELECT * FROM $TABLE_RETRO WHERE uuid = :retroUuid")
    fun getRetro(retroUuid: String): Retro

    @Update
    fun updateRetro(retroUuid: Retro)

    @Transaction
    fun dropStatementsAndInsert(statements: List<Statement>) {
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