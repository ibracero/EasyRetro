package com.ibracero.retrum.ui.retros

import androidx.lifecycle.viewModelScope
import com.ibracero.retrum.common.BaseViewModel
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.RetroRepository
import com.ibracero.retrum.ui.FailureMessage
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
            retroRepository.getRetros().fold({ failure ->
                viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.NotFetched)
                viewEffect = failure.toViewEffect()
            }, {
                viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.Fetched(retros = it))
            })
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
