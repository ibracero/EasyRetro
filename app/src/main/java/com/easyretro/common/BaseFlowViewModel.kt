package com.easyretro.common

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class BaseFlowViewModel<UiState, UiEffect, UiEvent> : ViewModel(), ViewModelFlowContract<UiEvent> {

    val currentState: UiState
        get() = viewStates.value

    private val initialState: UiState by lazy { createInitialState() }

    protected open val viewStates: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    protected open val viewEffects: MutableSharedFlow<UiEffect> = MutableSharedFlow()

    abstract fun createInitialState(): UiState

    fun viewStates(): StateFlow<UiState> = viewStates.asStateFlow()

    fun viewEffects(): SharedFlow<UiEffect> = viewEffects.asSharedFlow()

    @CallSuper
    override fun process(uiEvent: UiEvent) {
        Timber.d("processing viewEvent: $uiEvent")
    }

    protected fun emitUiState(reduce: UiState.() -> UiState) {
        viewModelScope.launch {
            viewStates.value = currentState.reduce()
        }
    }

    protected fun emitUiEffect(uiEffect: UiEffect) {
        viewModelScope.launch {
            viewEffects.emit(uiEffect)
        }
    }
}

interface UiState
interface UiEvent
interface UiEffect

internal interface ViewModelFlowContract<UiEvent> {
    fun process(uiEvent: UiEvent)
}
