package com.easyretro.common

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber


open class BaseViewModel<STATE, EFFECT, EVENT> : ViewModel(), ViewModelContract<EVENT> {

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
            Timber.d("setting viewEffect : $value")
            _viewEffect = value
            viewEffects.value = value
        }

    private val viewStates: MutableLiveData<STATE> = MutableLiveData()
    private val viewEffects: SingleLiveEvent<EFFECT> = SingleLiveEvent()

    private var _viewState: STATE? = null
    private var _viewEffect: EFFECT? = null

    fun viewStates(): LiveData<STATE> = viewStates

    fun viewEffects(): SingleLiveEvent<EFFECT> = viewEffects

    @CallSuper
    override fun process(viewEvent: EVENT) {
        Timber.d("processing viewEvent: $viewEvent")
    }
}

internal interface ViewModelContract<EVENT> {
    fun process(uiEvent: EVENT)
}
