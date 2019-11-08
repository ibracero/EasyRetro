package com.ibracero.retrum.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TABLE_USER = "user"
const val TABLE_RETRO = "retro"
const val TABLE_STATEMENT = "statement"

@Entity(tableName = TABLE_USER)
data class User(
    @PrimaryKey val uuid: String,
    val firstName: String,
    val lastName: String,
    val retroUuids: List<String>
)

@Entity(tableName = TABLE_RETRO)
data class Retro(
    @PrimaryKey val uuid: String,
    val title: String,
    val positivePoints: List<String>,
    val negativePoints: List<String>,
    val actionPoints: List<String>
)

@Entity(tableName = TABLE_STATEMENT)
data class Statement(
    @PrimaryKey val uuid: String,
    val userEmail: String,
    val description: String
)
