package com.easyretro.ui.board.negative

import android.os.Bundle
import com.easyretro.domain.StatementType
import com.easyretro.ui.board.BoardFragment
import com.easyretro.ui.board.StatementFragment

class NegativeFragment(override val statementType: StatementType = StatementType.NEGATIVE) : StatementFragment() {
    companion object {
        fun newInstance(retroUuid: String?): NegativeFragment {
            val fragment = NegativeFragment()
            fragment.arguments = Bundle().apply {
                putString(retroUuid, BoardFragment.ARGUMENT_RETRO_UUID)
            }
            return fragment
        }
    }
}