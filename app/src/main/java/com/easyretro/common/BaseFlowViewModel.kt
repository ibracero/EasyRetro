package com.easyretro.common

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


open class BaseFlowViewModel<STATE, EFFECT, EVENT>(
    initialViewState: STATE
) : ViewModel(), ViewModelContract<EVENT> {

    protected var viewState: STATE
        get() = _viewState
            ?: throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")
        set(value) {
            Timber.d("setting viewState : $value")
            _viewState = value
            viewStates.value = value
        }

    protected var viewEffect: EFFECT
        get() = _viewEffect
            ?: throw UninitializedPropertyAccessException("\"viewEffect\" was queried before being initialized")
        set(value) {
            viewModelScope.launch {
                Timber.d("setting viewEffect : $value")
                _viewEffect = value
                viewEffects.emit(value)
            }
        }

    private val viewStates: MutableStateFlow<STATE> = MutableStateFlow(initialViewState)
    private val viewEffects: MutableSharedFlow<EFFECT> = MutableSharedFlow()

    private var _viewState: STATE? = null
    private var _viewEffect: EFFECT? = null

    fun viewStates(): StateFlow<STATE> = viewStates.asStateFlow()

    fun viewEffects(): SharedFlow<EFFECT> = viewEffects.asSharedFlow()

    @CallSuper
    override fun process(viewEvent: EVENT) {
        Timber.d("processing viewEvent: $viewEvent")
    }
}

internal interface ViewModelFlowContract<EVENT> {
    fun process(viewEvent: EVENT)
}
