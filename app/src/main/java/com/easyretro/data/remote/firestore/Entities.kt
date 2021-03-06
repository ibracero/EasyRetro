package com.easyretro.data.remote.firestore

data class RetroRemote constructor(
    val uuid: String,
    val title: String,
    val timestamp: Long? = null,
    val ownerEmail: String? = null,
    val protected: Boolean = false,
    val deepLink: String? = null,
    val users: List<UserRemote>? = emptyList()
)

data class StatementRemote(
    val uuid: String? = null,
    val retroUuid: String? = null,
    val userEmail: String,
    val description: String,
    val statementType: String,
    val timestamp: Long? = null,
    val isRemovable: Boolean? = false
)

data class UserRemote(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val photoUrl: String? = null,
    val retroUuids: List<RetroRemote>? = null
)