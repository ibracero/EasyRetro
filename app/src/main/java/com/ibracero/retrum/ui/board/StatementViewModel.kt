package com.ibracero.retrum.ui.board

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class StatementViewModel(
    private val repository: Repository
) : ViewModel() {

    fun getStatements(retroUuid: String, type: StatementType) = repository.getStatements(retroUuid, type)

    fun addStatement(retroUuid: String, description: String, type: StatementType) {
        repository.addStatement(retroUuid = retroUuid, description = description, statementType = type)
    }

    override fun onCleared() {
        super.onCleared()
        repository.dispose()
    }
}