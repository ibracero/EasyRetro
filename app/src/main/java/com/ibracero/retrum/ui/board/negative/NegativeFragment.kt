package com.ibracero.retrum.ui.board.negative

import android.os.Bundle
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.ui.board.BoardFragment
import com.ibracero.retrum.ui.board.StatementFragment

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