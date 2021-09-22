package com.easyretro.ui.retros.adapter

import android.view.ViewGroup
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.inflate
import com.easyretro.databinding.ItemRetroBinding
import com.easyretro.domain.model.Retro

class RetroViewHolder(
    parent: ViewGroup,
    val onClick: (Retro) -> Unit
) : BaseViewHolder(parent.inflate(R.layout.item_retro)) {

    private val binding = ItemRetroBinding.bind(itemView)

    fun bindTo(retro: Retro) {
        binding.retroTitle.text = retro.title
        itemView.setOnClickListener { onClick(retro) }
    }
}