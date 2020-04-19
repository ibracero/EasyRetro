package com.easyretro.ui.board

import androidx.lifecycle.viewModelScope
import com.easyretro.common.BaseViewModel
import com.easyretro.common.extensions.exhaustive
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.RetroRepository
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType
import com.easyretro.ui.FailureMessage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class StatementViewModel(
    private val boardRepository: BoardRepository
) : BaseViewModel<StatementListViewState, StatementListViewEffect, StatementListViewEvent>() {

    init {
        viewState = StatementListViewState(statements = emptyList(), addState = StatementAddState.None)
    }

    override fun process(viewEvent: StatementListViewEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is StatementListViewEvent.FetchStatements ->
                fetchStatements(retroUuid = viewEvent.retroUuid, type = viewEvent.type)
            is StatementListViewEvent.RemoveStatement ->
                removeStatement(statement = viewEvent.statement)
            is StatementListViewEvent.AddStatement ->
                addStatement(
                    retroUuid = viewEvent.retroUuid,
                    description = viewEvent.description,
                    type = viewEvent.type
                )
        }.exhaustive
    }

    private fun fetchStatements(retroUuid: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.getStatements(retroUuid, type)
                .collect {
                    viewState = viewState.copy(statements = it)
                }
        }
    }

    private fun addStatement(retroUuid: String, description: String, type: StatementType) {
        viewModelScope.launch {
            boardRepository.addStatement(retroUuid = retroUuid, description = description, type = type)
                .mapLeft {
                    viewEffect = StatementListViewEffect.ShowSnackBar(FailureMessage.parse(it))
                    viewState = viewState.copy(addState = StatementAddState.NotAdded)
                }
        }
    }

    private fun removeStatement(statement: Statement) {
        viewModelScope.launch {
            boardRepository.removeStatement(statement)
                .mapLeft {
                    viewEffect = StatementListViewEffect.ShowSnackBar(FailureMessage.parse(it))
                }
        }
    }
}