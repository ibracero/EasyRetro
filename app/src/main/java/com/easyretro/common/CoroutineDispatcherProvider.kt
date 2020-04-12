package com.easyretro.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CoroutineDispatcherProvider {
    val main: CoroutineContext = Dispatchers.Main
    val io: CoroutineContext = Dispatchers.IO
}