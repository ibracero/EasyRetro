package com.easyretro.ui.board

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easyretro.R
import com.easyretro.common.extensions.getColor
import com.easyretro.common.extensions.inflate
import com.easyretro.common.extensions.visibleOrGone
import com.easyretro.databinding.ItemStatementBinding
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType

class StatementViewHolder(
    parent: ViewGroup,
    val onRemoveClicked: (Statement) -> Unit
) : RecyclerView.ViewHolder(parent.inflate(R.layout.item_statement)) {

    private val binding = ItemStatementBinding.bind(itemView)

    fun bindTo(statement: Statement) {
        with(binding) {
            userEmail.text = statement.userEmail
            description.text = statement.description
            actionDelete.visibleOrGone(statement.removable)
            actionDelete.setOnClickListener { onRemoveClicked(statement) }
            val color = when (statement.type) {
                StatementType.POSITIVE -> R.color.positiveBackgroundColor
                StatementType.NEGATIVE -> R.color.negativeBackgroundColor
                StatementType.ACTION_POINT -> root.getColor(R.color.actionsBackgroundColor)
            }

            statementCard.setCardBackgroundColor(root.getColor(color))
        }
    }

    fun onContentChanged(descriptionText: String, isRemovable: Boolean) {
        binding.description.text = descriptionText
        binding.actionDelete.visibleOrGone(isRemovable)
    }
}