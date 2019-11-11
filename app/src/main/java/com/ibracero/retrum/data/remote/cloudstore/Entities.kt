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