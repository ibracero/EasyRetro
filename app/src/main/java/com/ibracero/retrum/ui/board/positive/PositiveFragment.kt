package com.ibracero.retrum.ui.board.positive

import android.os.Bundle
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.ui.board.BoardFragment
import com.ibracero.retrum.ui.board.StatementFragment

class PositiveFragment(override val statementType: StatementType = StatementType.POSITIVE) : StatementFragment() {

    companion object {
        fun newInstance(retroUuid: String?): PositiveFragment {
            val fragment = PositiveFragment()
            fragment.arguments = Bundle().apply {
                putString(retroUuid, BoardFragment.ARGUMENT_RETRO_UUID)
            }
            return fragment
        }
    }
}