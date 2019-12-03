package com.ibracero.retrum.ui.board.action

import android.os.Bundle
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.ui.board.BoardFragment
import com.ibracero.retrum.ui.board.StatementFragment
import com.ibracero.retrum.ui.board.positive.PositiveFragment

class ActionsFragment(override val statementType: StatementType = StatementType.ACTION_POINT) : StatementFragment() {
    companion object {
        fun newInstance(retroUuid: String?): ActionsFragment {
            val fragment = ActionsFragment()
            fragment.arguments = Bundle().apply {
                putString(retroUuid, BoardFragment.ARGUMENT_RETRO_UUID)
            }
            return fragment
        }
    }
}