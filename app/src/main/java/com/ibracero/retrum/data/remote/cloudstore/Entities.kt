package com.ibracero.retrum.data.remote.cloudstore

data class RetroResponse(
    val uuid: String,
    val title: String,
    val positivePoints: List<StatementResponse>,
    val negativePoints: List<StatementResponse>,
    val actionPoints: List<StatementResponse>
)

data class StatementResponse(
    val uuid:String,
    val userEmail: String,
    val description: String
)