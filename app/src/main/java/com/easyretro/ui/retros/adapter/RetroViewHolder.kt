package com.easyretro.ui.retros.adapter

import android.view.ViewGroup
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.inflate
import com.easyretro.domain.model.Retro
import kotlinx.android.synthetic.main.item_retro.*

class RetroViewHolder(
    parent: ViewGroup,
    val onClick: (Retro) -> Unit
) : BaseViewHolder(parent.inflate(R.layout.item_retro)) {

    fun bindTo(retro: Retro) {
        retro_title.text = retro.title
        itemView.setOnClickListener { onClick(retro) }
    }
}