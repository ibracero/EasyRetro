package com.ibracero.retrum.ui.retros

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.Repository

class RetroListViewModel(
    private val repository: Repository
) : ViewModel() {

    val retroLiveData: LiveData<List<Retro>> = repository.getRetros()

    fun createRetro(title: String) = repository.createRetro(title)

    override fun onCleared() {
        super.onCleared()
        repository.dispose()
    }
}
