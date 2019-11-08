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
    private val repository: Repository,
    dispatchers: CoroutineDispatcherProvider
) : ViewModel() {

    private val job = Job()
    private val coroutineContext = job + dispatchers.main
    private val scope = CoroutineScope(coroutineContext)

    val positivePoints = repository.getStatements(StatementType.POSITIVE)

    fun openRetro() {
/*        scope.launch {
            positivePoints.value = State.Loading
            repository.loadRetro()
        }*/
        repository.loadRetro()
    }

    fun addPositivePoint(positivePoint: String) {
        scope.launch {
            repository.addStatement(StatementType.POSITIVE, positivePoint)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}