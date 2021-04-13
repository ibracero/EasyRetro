package com.easyretro.ui.retros

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.easyretro.analytics.events.RetroCreatedEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseViewModel
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.ui.FailureMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetroListViewModel @Inject constructor(
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
            is RetroListViewEvent.RetroClicked -> openRetro(viewEvent.retroUuid)
            is RetroListViewEvent.CreateRetroClicked -> createRetro(viewEvent.retroTitle)
            RetroListViewEvent.LogoutClicked -> logout()
        }
    }

    private fun fetchRetros() {
        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.Loading)
        viewModelScope.launch {
            retroRepository.getRetros()
                .collect {
                    it.fold({ failure ->
                        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.NotFetched)
                        viewEffect = failure.toViewEffect()
                    }, { retros ->
                        viewState = viewState.copy(fetchRetrosStatus = FetchRetrosStatus.Fetched(retros = retros))
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
                reportAnalytics(event = RetroCreatedEvent)
                viewState = viewState.copy(retroCreationStatus = RetroCreationStatus.Created)
                viewEffect = RetroListViewEffect.OpenRetroDetail(retroUuid = retro.uuid)
            })
        }
    }

    private fun openRetro(retroUuid: String) {
        viewEffect = RetroListViewEffect.OpenRetroDetail(retroUuid = retroUuid)
    }

    private fun logout() {
        viewModelScope.launch {
            accountRepository.logOut()
        }
    }

    private fun Failure.toViewEffect() = RetroListViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(this))
}
