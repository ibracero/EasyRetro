package com.ibracero.retrum.ui.retros

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.Repository

class RetroListViewModel(
    repository: Repository
) : ViewModel() {

    val retroLiveData : LiveData<List<Retro>> = repository.getRetros()
}
