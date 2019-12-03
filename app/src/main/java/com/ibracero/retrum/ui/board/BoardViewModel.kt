package com.ibracero.retrum.ui.board

import androidx.lifecycle.ViewModel
import com.ibracero.retrum.domain.BoardRepository

class BoardViewModel(private val boardRepository: BoardRepository) : ViewModel() {

    fun getRetroInfo(retroUuid: String) = boardRepository.getRetroInfo(retroUuid)
}