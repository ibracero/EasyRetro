package com.easyretro.common

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

abstract class BaseFlowFragment<STATE, EFFECT, EVENT, ViewModel : BaseFlowViewModel<STATE, EFFECT, EVENT>>(
    @LayoutRes layout: Int
) : Fragment(layout) {

    abstract val viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.viewStates().collectLatest {
                renderViewState(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.viewEffects().collectLatest {
                renderViewEffect(it)
            }
        }
    }

    abstract fun renderViewState(viewState: STATE)

    abstract fun renderViewEffect(viewEffect: EFFECT)
}