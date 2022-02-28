package com.easyretro.common

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

abstract class BaseFlowFragment<UiState, UiEffect, UiEvent, ViewModel : BaseFlowViewModel<UiState, UiEffect, UiEvent>>(
    @LayoutRes layout: Int
) : Fragment(layout) {

    abstract val viewModel: ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewStates().collectLatest {
                renderViewState(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewEffects().collectLatest {
                renderViewEffect(it)
            }
        }
    }

    abstract fun renderViewState(uiState: UiState)

    abstract fun renderViewEffect(uiEffect: UiEffect)
}