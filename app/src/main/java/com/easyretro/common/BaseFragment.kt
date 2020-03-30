package com.easyretro.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import timber.log.Timber

abstract class BaseFragment<STATE, EFFECT, EVENT, ViewModel : BaseViewModel<STATE, EFFECT, EVENT>> : Fragment() {

    abstract val viewModel: ViewModel

    private val viewStateObserver = Observer<STATE> {
        Timber.d("observed viewState : $it")
        renderViewState(it)
    }

    private val viewEffectObserver = Observer<EFFECT> {
        Timber.d("observed viewEffect : $it")
        renderViewEffect(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.viewStates().observe(this, viewStateObserver)
        viewModel.viewEffects().observe(this, viewEffectObserver)
    }

    abstract fun renderViewState(viewState: STATE)

    abstract fun renderViewEffect(viewEffect: EFFECT)
}