package com.easyretro.ui.retros

import androidx.lifecycle.viewModelScope
import com.easyretro.analytics.events.RetroCreatedEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.ui.retros.RetroListContract.*
import com.easyretro.domain.AccountRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.ui.FailureMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetroListViewModel @Inject constructor(
    private val retroRepository: RetroRepository,
    private val accountRepository: AccountRepository
) : BaseFlowViewModel<State, Effect, Event>() {

    override fun createInitialState(): State = State(RetroListState.Loading, NewRetroState.AddRetroShown)

    override fun process(uiEvent: Event) {
        super.process(uiEvent)
        when (uiEvent) {
            Event.ScreenLoaded -> fetchRetros()
            is Event.CreateRetroClicked -> createRetro(uiEvent.retroTitle)
            is Event.RetroClicked -> openRetro(uiEvent.retroUuid)
            Event.LogoutClicked -> logout()
        }
    }

    private fun fetchRetros() {
        viewModelScope.launch {
            emitUiState { copy(retroListState = RetroListState.Loading) }
            retroRepository.getRetros()
                .collect {
                    it.fold({ failure ->
                        emitUiState { copy(retroListState = RetroListState.ShowRetroList(emptyList())) }
                        emitUiEffect(failure.toUiEffect())
                    }, { retros ->
                        emitUiState { copy(retroListState = RetroListState.ShowRetroList(retros)) }
                    })
                }
        }
    }

    private fun createRetro(title: String) {
        viewModelScope.launch {
            emitUiState { copy(newRetroState = NewRetroState.Loading) }
            retroRepository.createRetro(title).fold({ failure ->
                emitUiState { copy(newRetroState = NewRetroState.TextInputShown) }
                emitUiEffect(failure.toUiEffect())
            }, { retro ->
                reportAnalytics(event = RetroCreatedEvent)
                emitUiState { copy(newRetroState = NewRetroState.AddRetroShown) }
                openRetro(retro.uuid)
            })
        }
    }

    private fun openRetro(retroUuid: String) {
        emitUiEffect(Effect.OpenRetroDetail(retroUuid))
    }

    private fun logout() {
        viewModelScope.launch {
            accountRepository.logOut()
        }
    }

    private fun Failure.toUiEffect() = Effect.ShowSnackBar(errorMessage = FailureMessage.parse(this))
}
