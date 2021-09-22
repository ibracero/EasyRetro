package com.easyretro.ui.board

import android.view.ViewGroup
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.getColor
import com.easyretro.common.extensions.inflate
import com.easyretro.common.extensions.visibleOrGone
import com.easyretro.databinding.ItemStatementBinding
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType

class StatementViewHolder(
    parent: ViewGroup,
    val onRemoveClicked: (Statement) -> Unit
) : BaseViewHolder(parent.inflate(R.layout.item_statement)) {

    private val binding = ItemStatementBinding.bind(itemView)

    fun bindTo(statement: Statement) {
        with(binding) {
            userEmail.text = statement.userEmail
            description.text = statement.description
            actionDelete.visibleOrGone(statement.removable)
            actionDelete.setOnClickListener { onRemoveClicked(statement) }
            when (statement.type) {
                StatementType.POSITIVE -> statementCard.setCardBackgroundColor(itemView.getColor(R.color.positiveBackgroundColor))
                StatementType.NEGATIVE -> statementCard.setCardBackgroundColor(itemView.getColor(R.color.negativeBackgroundColor))
                StatementType.ACTION_POINT -> statementCard.setCardBackgroundColor(itemView.getColor(R.color.actionsBackgroundColor))
            }
        }
    }

    fun onContentChanged(descriptionText: String, isRemovable: Boolean) {
        binding.description.text = descriptionText
        binding.actionDelete.visibleOrGone(isRemovable)
    }
}