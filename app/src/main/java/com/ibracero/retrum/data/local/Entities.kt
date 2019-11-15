package com.ibracero.retrum.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ibracero.retrum.domain.StatementType
import java.io.Serializable

const val TABLE_USER = "user"
const val TABLE_RETRO = "retro"
const val TABLE_STATEMENT = "statement"

@Entity(tableName = TABLE_USER)
data class User(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val retroUuids: List<String>
)

@Entity(tableName = TABLE_RETRO)
data class Retro(
    @PrimaryKey val uuid: String,
    val title: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = TABLE_STATEMENT)
data class Statement(
    @PrimaryKey val uuid: String,
    val retroUuid: String,
    val type: StatementType,
    val userEmail: String,
    val description: String,
    val timestamp: Long,
    val removable: Boolean
)
