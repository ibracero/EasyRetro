package com.ibracero.retrum.ui.retros

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.Repository

class RetroListViewModel(
    private val repository: Repository,
    connectionManager: RetrumConnectionManager
) : ViewModel() {

    val retroLiveData: LiveData<List<Retro>> = repository.getRetros()

    val connectivityLiveData = connectionManager.connectionLiveData

    fun createRetro(title: String) = repository.createRetro(title)

    fun startObservingRetros() {
        repository.startObservingUserRetros()
    }

    fun stopObservingRetros() {
        repository.stopObservingUserRetros()
    }
}
