package com.easyretro.ui.board

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.StatementType
import com.easyretro.ui.FailureMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatementViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val retroRepository: RetroRepository
) : BaseViewModel<StatementListViewState, StatementListViewEffect, StatementListViewEvent>() {

    init {
        viewState = StatementListViewState(statements = emptyList(), addState = StatementAddState.Hidden)
    }

    override fun process(viewEvent: StatementListViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is StatementListViewEvent.CheckRetroLock -> checkRetroLock(retroUuid = viewEvent.retroUuid)
            is StatementListViewEvent.RemoveStatement ->
                removeStatement(retroUuid = viewEvent.retroUuid, statementUuid = viewEvent.statementUuid)
            is StatementListViewEvent.AddStatement ->
                addStatement(
                    retroUuid = viewEvent.retroUuid,
                    description = viewEvent.description,
                    type = viewEvent.type
                )
            is StatementListViewEvent.FetchStatements ->
                fetchStatements(retroUuid = viewEvent.retroUuid, type = viewEvent.type)
        }.exhaustive
    }

    private fun fetchStatements(retroUuid: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.getStatements(retroUuid = retroUuid, statementType = type)
                .collect {
                    viewState = viewState.copy(statements = it)
                }
        }
    }

    private fun checkRetroLock(retroUuid: String) {
        viewModelScope.launch {
            retroRepository.observeRetro(retroUuid = retroUuid)
                .collect {
                    it.fold({ failure ->
                        viewEffect = StatementListViewEffect.ShowSnackBar(errorMessage = FailureMessage.parse(failure))
                    }, { retro ->
                        viewState = viewState.copy(
                            addState = if (retro.protected) StatementAddState.Hidden else StatementAddState.Shown
                        )
                    })
                }
        }
    }

    private fun addStatement(retroUuid: String, description: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.addStatement(retroUuid = retroUuid, description = description, type = type)
                .fold({
                    viewEffect = StatementListViewEffect.ShowSnackBar(FailureMessage.parse(it))
                    viewEffect = StatementListViewEffect.CreateItemFailed
                }, {
                    viewEffect = StatementListViewEffect.CreateItemSuccess
                })
        }
    }

    private fun removeStatement(retroUuid: String, statementUuid: String) {
        viewModelScope.launch {
            boardRepository.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)
                .mapLeft {
                    viewEffect = StatementListViewEffect.ShowSnackBar(FailureMessage.parse(it))
                }
        }
    }
}