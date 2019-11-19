package com.ibracero.retrum.ui.board

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.domain.StatementType

class StatementViewModel(
    private val boardRepository: BoardRepository
) : ViewModel() {

    fun getStatements(retroUuid: String, type: StatementType) = boardRepository.getStatements(retroUuid, type)

    fun addStatement(retroUuid: String, description: String, type: StatementType) {
        boardRepository.addStatement(retroUuid = retroUuid, description = description, statementType = type)
    }

    override fun onCleared() {
        super.onCleared()
        boardRepository.dispose()
    }

    fun removeStatement(statement: Statement) {
        boardRepository.removeStatement(statement)
    }
}