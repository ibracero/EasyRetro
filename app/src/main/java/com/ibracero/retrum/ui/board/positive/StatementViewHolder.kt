package com.ibracero.retrum.ui.board.positive

import android.view.ViewGroup
import com.ibracero.retrum.R
import com.ibracero.retrum.common.CachedRecyclerViewHolder
import com.ibracero.retrum.common.inflate
import com.ibracero.retrum.data.local.Statement
import kotlinx.android.synthetic.main.item_statement.view.*

class StatementViewHolder(parent: ViewGroup) : CachedRecyclerViewHolder(parent.inflate(R.layout.item_statement)) {
    fun bindTo(statement: Statement) {
        containerView.user_email.text = statement.userEmail
        containerView.description.text = statement.description
    }
}