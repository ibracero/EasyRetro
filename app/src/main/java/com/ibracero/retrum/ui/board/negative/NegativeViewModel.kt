package com.ibracero.retrum.ui.board.negative

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class NegativeViewModel(
    private val repository: Repository
) : ViewModel() {

    fun getNegativePoints(retroUuid: String) = repository.getStatements(retroUuid, StatementType.NEGATIVE)

    fun addStatement(description: String) {
        repository.addStatement()
    }

    override fun onCleared() {
        super.onCleared()
        repository.dispose()
    }
}