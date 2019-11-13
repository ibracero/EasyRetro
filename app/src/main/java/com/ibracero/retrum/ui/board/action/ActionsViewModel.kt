package com.ibracero.retrum.ui.board.action

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class ActionsViewModel(
    private val repository: Repository
) : ViewModel() {

    fun getActionPoints(retroUuid: String) = repository.getStatements(retroUuid, StatementType.ACTION_POINT)

    fun addStatement(description: String) {
        repository.addStatement()
    }

    override fun onCleared() {
        super.onCleared()
        repository.dispose()
    }
}