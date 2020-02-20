package com.ibracero.retrum.ui.retros

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.domain.RetroRepository

class RetroListViewModel(
    private val retroRepository: RetroRepository,
    private val accountRepository: AccountRepository,
    connectionManager: RetrumConnectionManager
) : ViewModel() {

    val retroLiveData: LiveData<List<Retro>> = retroRepository.getRetros()

    val connectivityLiveData = connectionManager.connectionLiveData

    fun createRetro(title: String) = retroRepository.createRetro(title)

    fun logout() {
        accountRepository.logOut()
    }

    fun startObservingRetros() {
        retroRepository.startObservingUserRetros()
    }

    fun stopObservingRetros() {
        retroRepository.stopObservingUserRetros()
    }

    override fun onCleared() {
        super.onCleared()
        retroRepository.dispose()
    }
}
