package com.ibracero.retrum.ui.retros

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.ui.AddItemViewHolder

class RetroListAdapter(
    private val onRetroClicked: (Retro) -> Unit,
    private val onAddClicked: (String) -> Unit
) : ListAdapter<Retro, RecyclerView.ViewHolder>(RetroDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ADD = 1
        private const val VIEW_TYPE_RETRO = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddItemViewHolder(parent, onAddClicked)
            else -> RetroViewHolder(parent, onRetroClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RetroViewHolder -> holder.bindTo(getItem(position))
            else -> Unit
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ADD
            else -> VIEW_TYPE_RETRO
        }
    }

    class RetroDiffCallback : DiffUtil.ItemCallback<Retro>() {
        override fun areItemsTheSame(oldItem: Retro, newItem: Retro): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areContentsTheSame(oldItem: Retro, newItem: Retro): Boolean =
            oldItem == newItem
    }
}

