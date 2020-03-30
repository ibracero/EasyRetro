package com.easyretro.data.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.User
import com.easyretro.data.remote.firestore.UserRemote

class UserRemoteToDomainMapper : Mapper<UserRemote, User> {

    override fun map(from: UserRemote): User =
        User(
            email = from.email.orEmpty(),
            firstName = from.firstName.orEmpty(),
            lastName = from.lastName.orEmpty(),
            photoUrl = from.photoUrl.orEmpty()
        )
}