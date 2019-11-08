package com.ibracero.retrum.ui.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
        repository.addStatement(StatementType.POSITIVE, positivePoint)
    }
}