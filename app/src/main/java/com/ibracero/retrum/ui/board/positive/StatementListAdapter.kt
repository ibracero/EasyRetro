package com.ibracero.retrum.ui.board.positive

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ibracero.retrum.data.local.Statement

class StatementListAdapter : ListAdapter<Statement, StatementViewHolder>(StatementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatementViewHolder =
        StatementViewHolder(parent)

    override fun onBindViewHolder(holder: StatementViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class StatementDiffCallback : DiffUtil.ItemCallback<Statement>() {
        override fun areContentsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areItemsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem == newItem
    }
}