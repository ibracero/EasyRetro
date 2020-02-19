package com.ibracero.retrum.ui.board

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.RetroRepository

class BoardViewModel(private val retroRepository: RetroRepository) : ViewModel() {

    fun getRetroInfo(retroUuid: String) = retroRepository.getRetro(retroUuid)
}