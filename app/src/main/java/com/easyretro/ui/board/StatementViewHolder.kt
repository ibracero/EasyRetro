package com.easyretro.ui.board

import android.view.ViewGroup
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.getColor
import com.easyretro.common.extensions.inflate
import com.easyretro.common.extensions.visibleOrGone
import com.easyretro.data.local.Statement
import com.easyretro.domain.StatementType
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