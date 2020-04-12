package com.easyretro.domain

import com.easyretro.data.local.Retro

val retroUuid = "retro-uuid"
val retroTitle = "Retro title"
val domainRetro = Retro(
    uuid = retroUuid,
    title = retroTitle,
    timestamp = 1000L,
    users = emptyList()
)