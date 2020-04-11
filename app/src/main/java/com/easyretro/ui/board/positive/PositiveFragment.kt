package com.easyretro.ui.board.positive

import android.os.Bundle
import com.easyretro.domain.StatementType
import com.easyretro.ui.board.BoardFragment
import com.easyretro.ui.board.StatementFragment

class PositiveFragment(override val statementType: StatementType = StatementType.POSITIVE) : StatementFragment() {

    companion object {
        fun newInstance(retroUuid: String?): PositiveFragment {
            val fragment = PositiveFragment()
            fragment.arguments = Bundle().apply {
                putString(BoardFragment.ARGUMENT_RETRO_UUID, retroUuid)
            }
            return fragment
        }
    }
}
