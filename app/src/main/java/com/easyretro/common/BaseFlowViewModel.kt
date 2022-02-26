package com.easyretro.common

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class BaseFlowViewModel<STATE, EFFECT, EVENT>(
    initialViewState: STATE
) : ViewModel(), ViewModelContract<EVENT> {

    abstract val dispatchers: CoroutineDispatcherProvider

    protected open val viewStates: MutableStateFlow<STATE> = MutableStateFlow(initialViewState)
    protected open val viewEffects: MutableSharedFlow<EFFECT> = MutableSharedFlow()

    fun viewStates(): StateFlow<STATE> = viewStates.asStateFlow()

    fun viewEffects(): SharedFlow<EFFECT> = viewEffects.asSharedFlow()

    @CallSuper
    override fun process(viewEvent: EVENT) {
        Timber.d("processing viewEvent: $viewEvent")
    }

    protected fun emitViewState(viewState: STATE) {
        viewModelScope.launch(dispatchers.main()) {
            viewStates.value = viewState
        }
    }

    protected fun emitViewEffect(viewEffect: EFFECT) {
        viewModelScope.launch(dispatchers.main()) {
            viewEffects.emit(viewEffect)
        }
    }
}

internal interface ViewModelFlowContract<EVENT> {
    fun process(viewEvent: EVENT)
}
