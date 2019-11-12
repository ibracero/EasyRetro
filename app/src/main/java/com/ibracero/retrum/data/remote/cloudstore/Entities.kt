package com.ibracero.retrum.data.remote.cloudstore

data class RetroRemote(
    val uuid: String,
    val title: String
)

data class StatementRemote(
    val uuid: String? = null,
    val retroUuid: String? = null,
    val userEmail: String,
    val description: String,
    val statementType: String
)

data class UserRemote(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val retroUuids: List<String>? = null
)