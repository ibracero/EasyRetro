package com.ibracero.retrum.ui

import android.view.ViewGroup
import com.ibracero.retrum.R
import com.ibracero.retrum.common.*
import kotlinx.android.synthetic.main.item_add.view.*

class AddItemViewHolder(
    parent: ViewGroup,
    private val onAddClicked: (String) -> Unit
) : CachedRecyclerViewHolder(parent.inflate(R.layout.item_add)) {

    init {
        containerView.setOnClickListener { showTitle() }
    }

    private fun showTitle() {
        with(containerView) {
            add_title.visible()
            add_title.showKeyboard()
            add_icon.setOnClickListener {
                if (add_title.text.isNotEmpty()) onAddClicked(add_title.text.toString())
            }
        }
    }
}