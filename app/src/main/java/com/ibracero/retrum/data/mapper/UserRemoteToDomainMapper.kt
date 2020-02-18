package com.ibracero.retrum.data.mapper

import com.ibracero.retrum.common.Mapper
import com.ibracero.retrum.data.local.User
import com.ibracero.retrum.data.remote.firestore.UserRemote

class UserRemoteToDomainMapper : Mapper<UserRemote, User> {

    override fun map(from: UserRemote): User =
        User(
            email = from.email.orEmpty(),
            firstName = from.firstName.orEmpty(),
            lastName = from.lastName.orEmpty()
        )
}