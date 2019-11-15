package com.ibracero.retrum.ui.board.action

import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.ui.board.StatementFragment

class ActionsFragment(override val statementType: StatementType = StatementType.ACTION_POINT) : StatementFragment()