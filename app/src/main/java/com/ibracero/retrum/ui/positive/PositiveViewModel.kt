package com.ibracero.retrum.ui.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.domain.Repository
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

    val positivePoints = MutableLiveData<State>()

    fun openRetro() {
        scope.launch {
            positivePoints.value = State.Loading
            repository.openRetro()
        }
    }

    fun addPositivePoint(positivePoint: String){
        scope.launch {
            repository.addPositivePoint(positivePoint)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}

sealed class State {
    object Loading : State()
    class PositivePointsUpdated(positivePoints: List<String>) : State()
    class Error(exception: Throwable) : State()
}