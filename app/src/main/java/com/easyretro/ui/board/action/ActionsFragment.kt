package com.easyretro.ui.board.action

import android.os.Bundle
import com.easyretro.domain.StatementType
import com.easyretro.ui.board.BoardFragment
import com.easyretro.ui.board.StatementFragment

class ActionsFragment(override val statementType: StatementType = StatementType.ACTION_POINT) : StatementFragment() {
    companion object {
        fun newInstance(retroUuid: String?): ActionsFragment {
            val fragment = ActionsFragment()
            fragment.arguments = Bundle().apply {
                putString(BoardFragment.ARGUMENT_RETRO_UUID, retroUuid)
            }
            return fragment
        }
    }
}