package com.easyretro.ui.retros

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.OffsetListAdapter
import com.easyretro.domain.model.Retro
import com.easyretro.ui.AddItemViewHolder
import com.easyretro.ui.Payload
import com.easyretro.ui.retros.adapter.RetroViewHolder

class RetroListAdapter(
    private val onRetroClicked: (Retro) -> Unit,
    private val onAddClicked: (String) -> Unit
) : OffsetListAdapter<Retro, BaseViewHolder>(RetroDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ADD = 1
        private const val VIEW_TYPE_RETRO = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ADD
            else -> VIEW_TYPE_RETRO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddItemViewHolder(
                parent,
                onAddClicked,
                AddItemViewHolder.ItemType.RETRO
            )
            else -> RetroViewHolder(parent, onRetroClicked)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is RetroViewHolder -> holder.bindTo(getItem(position))
            else -> Unit
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)

        payloads.forEach {
            when (it) {
                is Payload.CreateRetroPayload -> (holder as AddItemViewHolder).bindResult(success = it.success)
            }
        }
    }

    class RetroDiffCallback : DiffUtil.ItemCallback<Retro>() {
        override fun areItemsTheSame(oldItem: Retro, newItem: Retro): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areContentsTheSame(oldItem: Retro, newItem: Retro): Boolean =
            oldItem.title == newItem.title
    }
}

