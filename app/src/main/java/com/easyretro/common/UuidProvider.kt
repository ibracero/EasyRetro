package com.easyretro.common

import java.util.*

class UuidProvider {
    fun generateUuid() = UUID.randomUUID().toString()
}