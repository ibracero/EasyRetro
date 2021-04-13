package com.easyretro.data.remote.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.UserDb
import com.easyretro.data.remote.firestore.UserRemote
import javax.inject.Inject

class UserRemoteToDbMapper @Inject constructor() : Mapper<UserRemote, UserDb> {

    override fun map(from: UserRemote): UserDb =
        UserDb(
            email = from.email.orEmpty(),
            firstName = from.firstName.orEmpty(),
            lastName = from.lastName.orEmpty(),
            photoUrl = from.photoUrl.orEmpty()
        )
}