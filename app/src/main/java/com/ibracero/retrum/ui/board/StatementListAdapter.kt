package com.ibracero.retrum.ui.board

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibracero.retrum.common.OffsetListAdapter
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.ui.AddItemViewHolder
import com.ibracero.retrum.ui.retros.RetroListAdapter
import com.ibracero.retrum.ui.retros.RetroViewHolder

class StatementListAdapter(
    private val onAddClicked: (String) -> Unit
) : OffsetListAdapter<Statement, RecyclerView.ViewHolder>(StatementDiffCallback()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddItemViewHolder(parent, onAddClicked)
            else -> StatementViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StatementViewHolder -> holder.bindTo(getItem(position))
            else -> Unit
        }
    }

    class StatementDiffCallback : DiffUtil.ItemCallback<Statement>() {
        override fun areContentsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areItemsTheSame(oldItem: Statement, newItem: Statement): Boolean =
            oldItem == newItem
    }
}