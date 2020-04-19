package com.easyretro.ui.board

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.OffsetListAdapter
import com.easyretro.domain.model.Statement
import com.easyretro.ui.AddItemViewHolder
import com.easyretro.ui.Payload

class StatementListAdapter(
    private val onAddClicked: (String) -> Unit,
    private val onRemoveClick: (Statement) -> Unit
) : OffsetListAdapter<Statement, BaseViewHolder>(StatementDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ADD = 1
        private const val VIEW_TYPE_STATEMENT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ADD
            else -> VIEW_TYPE_STATEMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddItemViewHolder(parent, onAddClicked, AddItemViewHolder.ItemType.STATEMENT)
            else -> StatementViewHolder(parent, onRemoveClick)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is StatementViewHolder -> holder.bindTo(getItem(position))
            else -> Unit
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)

        payloads.forEach {
            when (it) {
                is Payload.CreateStatementPayload -> (holder as AddItemViewHolder).bindResult(success = it.success)
                is Payload.StatementContentPayload ->
                    (holder as StatementViewHolder).onContentChanged(
                        descriptionText = it.description,
                        isRemovable = it.isRemovable
                    )
            }
        }
    }

    class StatementDiffCallback : DiffUtil.ItemCallback<Statement>() {
        override fun areItemsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areContentsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem.hashCode() == newItem.hashCode()

        override fun getChangePayload(oldItem: Statement, newItem: Statement): Payload? {
            return Payload.StatementContentPayload(description = newItem.description, isRemovable = newItem.removable)
        }
    }
}