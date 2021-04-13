package com.easyretro.common

import java.util.*
import javax.inject.Inject

class UuidProvider @Inject constructor() {
    fun generateUuid() = UUID.randomUUID().toString()
}