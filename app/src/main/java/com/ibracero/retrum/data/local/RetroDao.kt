package com.ibracero.retrum.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ibracero.retrum.domain.StatementType

@Dao
interface RetroDao {

    @Query("SELECT * FROM $TABLE_RETRO")// AS r WHERE r.uuid IN (SELECT retroUuids FROM $TABLE_USER AS u WHERE u.uuid == :userUuid)
    fun getUserRetros(/*userUuid: String*/): LiveData<List<Retro>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'POSITIVE'")
    fun getPositiveStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'NEGATIVE'")
    fun getNegativeStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT WHERE retroUuid == :retroUuid AND type == 'ACTION_POINT'")
    fun getActionPoints(retroUuid: String): LiveData<List<Statement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRetros(retros: List<Retro>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStatements(statements: List<Statement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)
}