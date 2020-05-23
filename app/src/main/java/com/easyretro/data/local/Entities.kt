package com.easyretro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.easyretro.domain.model.StatementType

const val TABLE_USER = "user"
const val TABLE_RETRO = "retro"
const val TABLE_STATEMENT = "statement"

@Entity(tableName = TABLE_USER)
data class UserDb(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String
)

@Entity(tableName = TABLE_RETRO)
data class RetroDb(
    @PrimaryKey val uuid: String,
    val title: String,
    val timestamp: Long,
    val deepLink: String,
    val ownerEmail: String,
    val isProtected: Boolean,
    val users: List<UserDb>
)

@Entity(tableName = TABLE_STATEMENT)
data class StatementDb(
    @PrimaryKey val uuid: String,
    val retroUuid: String,
    val type: StatementType,
    val userEmail: String,
    val description: String,
    val timestamp: Long,
    val removable: Boolean
)
