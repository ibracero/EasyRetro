package com.easyretro.ui.retros

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseViewModel
import com.easyretro.data.local.Retro
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.Failure
import com.easyretro.domain.RetroRepository
import com.easyretro.ui.FailureMessage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RetroListViewModel(
    private val retroRepository: RetroRepository,
    private val accountRepository: AccountRepository
) : BaseViewModel<RetroListViewState, RetroListViewEffect, RetroListViewEvent>() {

    init {
        viewState = RetroListViewState(
            fetchRetrosStatus = FetchRetrosStatus.NotFetched,
            retroCreationStatus = RetroCreationStatus.NotCreated
        )
    }

    override fun process(viewEvent: RetroListViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            RetroListViewEvent.FetchRetros -> fetchRetros()
            is RetroListViewEvent.RetroClicked -> openRetro(viewEvent.retro)
            is RetroListViewEvent.CreateRetroClicked -> createRetro(viewEvent.retroName)
            RetroListViewEvent.LogoutClicked -> logout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        retroRepository.dispose()
    }

    private fun fetchRetros() {
        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.Loading)
        viewModelScope.launch {
            retroRepository.getRetros()
                .collect {
                    it.fold({ failure ->
                        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.NotFetched)
                        viewEffect = failure.toViewEffect()
                    }, {
                        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.Fetched(retros = it))
                    })
                }
        }
    }

    private fun createRetro(title: String) {
        viewState = viewState.copy(retroCreationStatus = RetroCreationStatus.Loading)
        viewModelScope.launch {
            retroRepository.createRetro(title).fold({ failure ->
                viewState = viewState.copy(retroCreationStatus = RetroCreationStatus.NotCreated)
                viewEffect = failure.toViewEffect()
            }, { retro ->
                viewState = viewState.copy(retroCreationStatus = RetroCreationStatus.Created)
                viewEffect = RetroListViewEffect.OpenRetroDetail(retroUuid = retro.uuid)
            })
        }
    }

    private fun openRetro(retro: Retro) {
        viewEffect = RetroListViewEffect.OpenRetroDetail(retroUuid = retro.uuid)
    }

    private fun logout() {
        viewModelScope.launch {
            accountRepository.logOut()
        }
    }

    private fun Failure.toViewEffect() = RetroListViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(this))
}
