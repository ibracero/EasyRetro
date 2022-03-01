package com.easyretro.common

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class BaseFlowViewModel<UiState, UiEffect, UiEvent> : ViewModel(), ViewModelContract<UiEvent> {

    private val initialState: UiState by lazy { createInitialState() }
    private val currentState: UiState
        get() = viewStates.value

    protected open val viewStates: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    protected open val viewEffects: MutableSharedFlow<UiEffect> = MutableSharedFlow()

    abstract fun createInitialState(): UiState

    fun viewStates(): StateFlow<UiState> = viewStates.asStateFlow()

    fun viewEffects(): SharedFlow<UiEffect> = viewEffects.asSharedFlow()

    @CallSuper
    override fun process(viewEvent: UiEvent) {
        Timber.d("processing viewEvent: $viewEvent")
    }

    protected fun emitViewState(reduce: UiState.() -> UiState) {
        viewModelScope.launch() {
            viewStates.value = currentState.reduce()
        }
    }

    protected fun emitViewEffect(viewEffect: UiEffect) {
        viewModelScope.launch() {
            viewEffects.emit(viewEffect)
        }
    }
}

interface UiState
interface UiEvent
interface UiEffect

internal interface ViewModelFlowContract<UiEvent> {
    fun process(viewEvent: UiEvent)
}
