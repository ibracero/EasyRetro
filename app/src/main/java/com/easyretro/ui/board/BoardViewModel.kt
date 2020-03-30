package com.easyretro.ui.board

import androidx.lifecycle.ViewModel
import com.easyretro.domain.RetroRepository

class BoardViewModel(private val retroRepository: RetroRepository) : ViewModel() {

    fun getRetroInfo(retroUuid: String) = retroRepository.getRetro(retroUuid)
}