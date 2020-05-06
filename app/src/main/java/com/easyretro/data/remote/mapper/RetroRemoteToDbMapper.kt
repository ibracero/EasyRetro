package com.easyretro.data.remote.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.RetroDb
import com.easyretro.data.remote.firestore.RetroRemote

class RetroRemoteToDbMapper(
    private val userRemoteToDbMapper: UserRemoteToDbMapper
) : Mapper<RetroRemote, RetroDb> {

    override fun map(from: RetroRemote): RetroDb =
        RetroDb(
            uuid = from.uuid,
            title = from.title,
            timestamp = from.timestamp ?: 0,
            ownerEmail = from.ownerEmail.orEmpty(),
            isProtected = from.locked ?: false,
            users = from.users?.map(userRemoteToDbMapper::map) ?: emptyList()
        )
}