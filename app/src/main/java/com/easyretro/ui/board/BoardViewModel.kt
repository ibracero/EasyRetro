package com.easyretro.ui.board

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Failure
import com.easyretro.ui.FailureMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BoardViewModel @ViewModelInject constructor(
    private val retroRepository: RetroRepository,
    private val boardRepository: BoardRepository
) : BaseViewModel<BoardViewState, BoardViewEffect, BoardViewEvent>() {

    private var statementObserverJob: Job? = null
    private var retroObserverJob: Job? = null

    override fun process(viewEvent: BoardViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BoardViewEvent.GetRetroInfo -> getRetroInfo(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.JoinRetro -> joinRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.ShareRetroLink -> shareRetroLink()
            is BoardViewEvent.SubscribeRetroDetails -> startObservingRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.ProtectRetro -> lockRetro(retroUuid = viewEvent.retroUuid)
            is BoardViewEvent.UnprotectRetro -> unlockRetro(retroUuid = viewEvent.retroUuid)
            BoardViewEvent.UnsubscribeRetroDetails -> stopObservingRetro()
        }.exhaustive
    }

    private fun getRetroInfo(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.observeRetro(retroUuid).collect { either ->
                either.fold(
                    {
                        viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                    }, {
                        viewState = BoardViewState(retro = it)
                    }
                )
            }
        }
    }

    private fun joinRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.joinRetro(retroUuid).mapLeft {
                viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
            }
        }
    }

    private fun shareRetroLink() {
        val retro = viewState.retro
        viewEffect = if (retro.deepLink.isNotEmpty())
            BoardViewEffect.ShowShareSheet(retroTitle = retro.title, deepLink = retro.deepLink)
        else BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(Failure.UnknownError))
    }

    private fun unlockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.unprotectRetro(retroUuid = retroUuid)
                .mapLeft {
                    viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                }
        }
    }

    private fun lockRetro(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.protectRetro(retroUuid = retroUuid)
                .mapLeft {
                    viewEffect = BoardViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(it))
                }
        }
    }

    private fun startObservingRetro(retroUuid: String) {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
        statementObserverJob = viewModelScope.launch {
            boardRepository.startObservingStatements(retroUuid).collect()//todo handle failure
        }
        retroObserverJob = viewModelScope.launch {
            retroRepository.startObservingRetroDetails(retroUuid).collect()//todo handle failure
        }
    }

    private fun stopObservingRetro() {
        statementObserverJob?.cancel()
        retroObserverJob?.cancel()
    }
}