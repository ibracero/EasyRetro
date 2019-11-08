package com.ibracero.retrum.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RetroDao {

    @Query("SELECT * FROM $TABLE_RETRO AS r WHERE r.uuid IN (SELECT retroUuids FROM $TABLE_USER AS u WHERE uuid == :userUuid)")
    fun getUserRetros(userUuid: String): LiveData<List<Retro>>

    @Query("SELECT * FROM $TABLE_STATEMENT AS s WHERE s.uuid IN (SELECT r.positivePoints FROM $TABLE_RETRO AS r WHERE r.uuid == :retroUuid)")
    fun getPositiveStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT AS s WHERE s.uuid IN (SELECT r.negativePoints FROM $TABLE_RETRO AS r WHERE r.uuid == :retroUuid)")
    fun getNegativeStatements(retroUuid: String): LiveData<List<Statement>>

    @Query("SELECT * FROM $TABLE_STATEMENT AS s WHERE s.uuid IN (SELECT r.actionPoints FROM $TABLE_RETRO AS r WHERE r.uuid == :retroUuid)")
    fun getActionPoints(retroUuid: String): LiveData<List<Statement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createOrUpdateRetro(retro: Retro)
}