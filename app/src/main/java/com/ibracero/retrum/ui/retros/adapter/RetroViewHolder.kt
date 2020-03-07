package com.ibracero.retrum.ui.retros.adapter

import android.view.ViewGroup
import com.ibracero.retrum.R
import com.ibracero.retrum.common.BaseViewHolder
import com.ibracero.retrum.common.extensions.inflate
import com.ibracero.retrum.data.local.Retro
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