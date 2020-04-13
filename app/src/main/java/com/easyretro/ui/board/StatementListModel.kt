package com.easyretro.ui.board

import androidx.annotation.StringRes
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType

data class StatementListViewState(val statements: List<Statement>, val addState: StatementAddState)

sealed class StatementListViewEvent {
    data class FetchStatements(val retroUuid: String, val type: StatementType) : StatementListViewEvent()
    data class RemoveStatement(val statement: Statement) : StatementListViewEvent()
    data class AddStatement(
        val retroUuid: String,
        val description: String,
        val type: StatementType
    ) : StatementListViewEvent()
}

sealed class StatementListViewEffect {
    data class ShowSnackBar(@StringRes val errorMessage: Int) : StatementListViewEffect()
}

sealed class StatementAddState {
    object None : StatementAddState()
    object Added : StatementAddState()
    object NotAdded : StatementAddState()
}