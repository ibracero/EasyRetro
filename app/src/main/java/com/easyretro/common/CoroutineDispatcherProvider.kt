package com.easyretro.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface CoroutineDispatcherProvider {
    fun main(): CoroutineDispatcher = Dispatchers.Main
    fun io(): CoroutineDispatcher = Dispatchers.IO
}

class DefaultCoroutineDispatcher : CoroutineDispatcherProvider