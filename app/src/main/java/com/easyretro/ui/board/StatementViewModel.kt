package com.easyretro.ui.board

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseFlowViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.StatementType
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.board.StatementListContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatementViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val retroRepository: RetroRepository
) : BaseFlowViewModel<State, Effect, Event>() {

    override fun createInitialState(): State = State(statements = emptyList(), addState = StatementAddState.Hidden)

    override fun process(uiEvent: Event) {
        super.process(uiEvent)
        when (uiEvent) {
            is Event.CheckRetroLock -> checkRetroLock(retroUuid = uiEvent.retroUuid)
            is Event.RemoveStatement ->
                removeStatement(retroUuid = uiEvent.retroUuid, statementUuid = uiEvent.statementUuid)
            is Event.AddStatement ->
                addStatement(
                    retroUuid = uiEvent.retroUuid,
                    description = uiEvent.description,
                    type = uiEvent.type
                )
            is Event.FetchStatements ->
                fetchStatements(retroUuid = uiEvent.retroUuid, type = uiEvent.type)
        }.exhaustive
    }

    private fun fetchStatements(retroUuid: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.getStatements(retroUuid = retroUuid, statementType = type)
                .collect {
                    emitUiState { copy(statements = it) }
                }
        }
    }

    private fun checkRetroLock(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.observeRetro(retroUuid = retroUuid)
                .collect {
                    it.fold({ failure ->
                        emitUiEffect(Effect.ShowSnackBar(errorMessage = FailureMessage.parse(failure)))
                    }, { retro ->
                        emitUiState { copy(addState = if (retro.protected) StatementAddState.Hidden else StatementAddState.Shown) }
                    })
                }
        }
    }

    private fun addStatement(retroUuid: String, description: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.addStatement(retroUuid = retroUuid, description = description, type = type)
                .fold({
                    emitUiEffect(Effect.ShowSnackBar(FailureMessage.parse(it)))
                    emitUiEffect(Effect.CreateItemFailed)
                }, {
                    emitUiEffect(Effect.CreateItemSuccess)
                })
        }
    }

    private fun removeStatement(retroUuid: String, statementUuid: String) {
        viewModelScope.launch {
            boardRepository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)
                .mapLeft {
                    emitUiEffect(Effect.ShowSnackBar(FailureMessage.parse(it)))
                }
        }
    }
}