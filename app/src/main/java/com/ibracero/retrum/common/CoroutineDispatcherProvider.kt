package com.ibracero.retrum.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class CoroutineDispatcherProvider {
    open val main: CoroutineContext = Dispatchers.Main
    open val io: CoroutineContext = Dispatchers.IO
}