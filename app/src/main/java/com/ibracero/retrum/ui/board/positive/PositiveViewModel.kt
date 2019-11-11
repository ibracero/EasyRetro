package com.ibracero.retrum.ui.board.positive

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class PositiveViewModel(
    private val repository: Repository
) : ViewModel() {

    val positivePoints = repository.getStatements(StatementType.POSITIVE)

    init {
        openRetro()
    }

    fun openRetro() {
        repository.loadRetro()
    }

    fun addPositivePoint(positivePoint: String) {
        repository.addStatement()
    }
}