package com.easyretro.domain.model

data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String
)

data class Retro(
    val uuid: String,
    val title: String,
    val timestamp: Long,
    val deepLink: String,
    val lockingAllowed: Boolean,
    val protected: Boolean,
    val users: List<User>
)

data class Statement(
    val uuid: String,
    val retroUuid: String,
    val type: StatementType,
    val userEmail: String,
    val description: String,
    val timestamp: Long,
    val removable: Boolean
)