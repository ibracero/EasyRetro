package com.ibracero.retrum.ui.retros

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibracero.retrum.R
import com.ibracero.retrum.common.CachedRecyclerViewHolder
import com.ibracero.retrum.common.inflate
import com.ibracero.retrum.data.local.Retro
import kotlinx.android.synthetic.main.item_retro.view.*

class RetroViewHolder(
    parent: ViewGroup,
    val onClick: (Retro) -> Unit
) : CachedRecyclerViewHolder(parent.inflate(R.layout.item_retro)) {

    fun bindTo(retro: Retro) {
        containerView.retro_title.text = retro.title
        containerView.setOnClickListener { onClick(retro) }
    }
}