package com.easyretro.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface CoroutineDispatcherProvider {
    fun main(): CoroutineContext = Dispatchers.Main
    fun io(): CoroutineContext = Dispatchers.IO
}

class DefaultCoroutineDispatcher : CoroutineDispatcherProvider