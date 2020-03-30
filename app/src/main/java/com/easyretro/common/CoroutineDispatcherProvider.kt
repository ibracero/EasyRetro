package com.easyretro.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class CoroutineDispatcherProvider {
    open val main: CoroutineContext = Dispatchers.Main
    open val io: CoroutineContext = Dispatchers.IO
}