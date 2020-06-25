package com.easyretro.data.local.mapper

import com.easyretro.common.Mapper
import com.easyretro.data.local.UserDb
import com.easyretro.domain.model.User
import javax.inject.Inject

class UserDbToDomainMapper @Inject constructor() : Mapper<UserDb, User> {

    override fun map(from: UserDb): User =
        User(
            email = from.email,
            firstName = from.firstName,
            lastName = from.lastName,
            photoUrl = from.photoUrl
        )
}