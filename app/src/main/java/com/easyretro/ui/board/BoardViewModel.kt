package com.easyretro.ui.board

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.board.BoardContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val retroRepository: RetroRepository,
    private val boardRepository: BoardRepository
) : BaseFlowViewModel<State, Effect, Event>() {

    private var statementObserverJob: Job? = null
    private var retroObserverJob: Job? = null

    override fun createInitialState(): State = State(RetroState.Loading)

    override fun process(uiEvent: Event) {
        super.process(uiEvent)
        when (uiEvent) {
            is Event.GetRetroInfo -> getRetroInfo(retroUuid = uiEvent.retroUuid)
            is Event.JoinRetro -> joinRetro(retroUuid = uiEvent.retroUuid)
            is Event.ShareRetroLink -> shareRetroLink()
            is Event.SubscribeRetroDetails -> startObservingRetro(retroUuid = uiEvent.retroUuid)
            is Event.ProtectRetro -> lockRetro(retroUuid = uiEvent.retroUuid)
            is Event.UnprotectRetro -> unlockRetro(retroUuid = uiEvent.retroUuid)
            Event.UnsubscribeRetroDetails -> stopObservingRetro()
        }.exhaustive
    }

    private fun getRetroInfo(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.observeRetro(retroUuid).collect { either ->
                either.fold(
                    {
                        emitUiEffect(Effect.ShowSnackBar(errorMessage = FailureMessage.parse(it)))
                    }, {
                        emitUiState { copy(retroState = RetroState.RetroLoaded(it)) }
                    }
                )
            }
        }
    }

    private fun joinRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.joinRetro(retroUuid).mapLeft {
                emitUiEffect(Effect.ShowSnackBar(errorMessage = FailureMessage.parse(it)))
            }
        }
    }

    private fun shareRetroLink() {
        val retroState = currentState.retroState
        emitUiEffect(
            if (retroState is RetroState.RetroLoaded && retroState.retro.deepLink.isNotEmpty())
                Effect.ShowShareSheet(retroTitle = retroState.retro.title, deepLink = retroState.retro.deepLink)
            else Effect.ShowSnackBar(errorMessage = FailureMessage.parse(Failure.UnknownError))
        )
    }

    private fun unlockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.unprotectRetro(retroUuid = retroUuid)
                .mapLeft {
                    emitUiEffect(Effect.ShowSnackBar(errorMessage = FailureMessage.parse(it)))
                }
        }
    }

    private fun lockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.protectRetro(retroUuid = retroUuid)
                .mapLeft {
                    emitUiEffect(Effect.ShowSnackBar(errorMessage = FailureMessage.parse(it)))
                }
        }
    }

    private fun startObservingRetro(retroUuid: String) {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
        statementObserverJob = viewModelScope.launch {
            boardRepository.startObservingStatements(retroUuid).collect()
        }
        retroObserverJob = viewModelScope.launch {
            retroRepository.startObservingRetroDetails(retroUuid).collect()
        }
    }

    private fun stopObservingRetro() {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
    }
}