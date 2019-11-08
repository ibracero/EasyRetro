package com.ibracero.retrum.data.remote.cloudstore

data class RetroResponse(
    val uuid: String,
    val title: String,
    val positivePoints: List<String>,
    val negativePoints: List<String>,
    val actionPoints: List<String>
)

data class StatementResponse(
    val uuid:String,
    val userEmail: String,
    val description: String
)