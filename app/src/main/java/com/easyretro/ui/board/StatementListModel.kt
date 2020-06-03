package com.easyretro.ui.board

import androidx.annotation.StringRes
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType

data class StatementListViewState(val statements: List<Statement>, val addState: StatementAddState)

sealed class StatementListViewEvent {
    data class FetchStatements(val retroUuid: String, val type: StatementType) : StatementListViewEvent()
    data class CheckRetroLock(val retroUuid: String) : StatementListViewEvent()
    data class RemoveStatement(val retroUuid: String, val statementUuid: String) : StatementListViewEvent()
    data class AddStatement(
        val retroUuid: String,
        val description: String,
        val type: StatementType
    ) : StatementListViewEvent()
}

sealed class StatementListViewEffect {
    data class ShowSnackBar(@StringRes val errorMessage: Int) : StatementListViewEffect()
    object CreateItemSuccess : StatementListViewEffect()
    object CreateItemFailed : StatementListViewEffect()
}

sealed class StatementAddState {
    object Shown : StatementAddState()
    object Hidden : StatementAddState()
}