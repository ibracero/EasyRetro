package com.easyretro.ui.board

import androidx.annotation.StringRes
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType

class StatementListContract {
    data class State(val statements: List<Statement>, val addState: StatementAddState)

    sealed class Event {
        data class FetchStatements(val retroUuid: String, val type: StatementType) : Event()
        data class CheckRetroLock(val retroUuid: String) : Event()
        data class RemoveStatement(val retroUuid: String, val statementUuid: String) : Event()
        data class AddStatement(
            val retroUuid: String,
            val description: String,
            val type: StatementType
        ) : Event()
    }

    sealed class Effect {
        data class ShowSnackBar(@StringRes val errorMessage: Int) : Effect()
        object CreateItemSuccess : Effect()
        object CreateItemFailed : Effect()
    }

    sealed class StatementAddState {
        object Shown : StatementAddState()
        object Hidden : StatementAddState()
    }
}