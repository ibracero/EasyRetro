package com.ibracero.retrum.ui.board.positive

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class PositiveViewModel(
    private val repository: Repository
) : ViewModel() {

    fun getPositivePoints(retroUuid: String) = repository.getStatements(retroUuid, StatementType.POSITIVE)

    fun addPositivePoint(positivePoint: String) {
        repository.addStatement()
    }

    override fun onCleared() {
        super.onCleared()
        repository.dispose()
    }
}