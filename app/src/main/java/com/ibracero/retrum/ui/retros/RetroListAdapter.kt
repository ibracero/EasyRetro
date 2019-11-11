package com.ibracero.retrum.ui.retros

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ibracero.retrum.data.local.Retro

class RetroListAdapter : ListAdapter<Retro, RetroViewHolder>(RetroDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetroViewHolder =
        RetroViewHolder(parent)

    override fun onBindViewHolder(holder: RetroViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class RetroDiffCallback : DiffUtil.ItemCallback<Retro>() {
    override fun areItemsTheSame(oldItem: Retro, newItem: Retro): Boolean =
        oldItem.uuid == newItem.uuid

    override fun areContentsTheSame(oldItem: Retro, newItem: Retro): Boolean =
        oldItem == newItem

}