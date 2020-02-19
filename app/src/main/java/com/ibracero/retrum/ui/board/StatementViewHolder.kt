package com.ibracero.retrum.ui.board

import android.view.ViewGroup
import com.ibracero.retrum.R
import com.ibracero.retrum.common.BaseViewHolder
import com.ibracero.retrum.common.getColor
import com.ibracero.retrum.common.inflate
import com.ibracero.retrum.common.visibleOrGone
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.domain.StatementType
import kotlinx.android.synthetic.main.item_statement.*

class StatementViewHolder(
    parent: ViewGroup,
    val onRemoveClicked: (Statement) -> Unit
) : BaseViewHolder(parent.inflate(R.layout.item_statement)) {

    fun bindTo(statement: Statement) {
        user_email.text = statement.userEmail
        description.text = statement.description
        action_delete.visibleOrGone(statement.removable)
        action_delete.setOnClickListener { onRemoveClicked(statement) }
        when (statement.type) {
            StatementType.POSITIVE -> statement_card.setCardBackgroundColor(containerView.getColor(R.color.positiveBackgroundColor))
            StatementType.NEGATIVE -> statement_card.setCardBackgroundColor(containerView.getColor(R.color.negativeBackgroundColor))
            StatementType.ACTION_POINT -> statement_card.setCardBackgroundColor(containerView.getColor(R.color.actionsBackgroundColor))
        }
    }

    fun onDescriptionChanged(descriptionText: String) {
        description.text = descriptionText
    }
}