package com.easyretro.data

import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.data.remote.firestore.UserRemote

val retroUuid = "retroUuid"
private val retroTitle = "retroTitle"
private val retroTimestamp = 1586705438L

val userEmail = "email@email.com"
private val userFirstName = "First name"
private val userLastName = "Last name"
private val userPhotoUrl = "photo.com/user1"
private val userOne = UserRemote(
    email = userEmail,
    firstName = userFirstName,
    lastName = userLastName,
    photoUrl = userPhotoUrl
)

val remoteRetro = RetroRemote(
    uuid = retroUuid,
    title = retroTitle,
    timestamp = retroTimestamp,
    users = listOf(userOne)
)