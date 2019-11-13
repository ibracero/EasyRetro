package com.ibracero.retrum.ui

import android.view.KeyEvent.KEYCODE_ENTER
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import com.ibracero.retrum.R
import com.ibracero.retrum.common.CachedRecyclerViewHolder
import com.ibracero.retrum.common.inflate
import com.ibracero.retrum.common.showKeyboard
import com.ibracero.retrum.common.visible
import kotlinx.android.synthetic.main.item_add.*

class AddItemViewHolder(
    parent: ViewGroup,
    private val onAddClicked: (String) -> Unit
) : CachedRecyclerViewHolder(parent.inflate(R.layout.item_add)) {
    init {
        containerView.setOnClickListener { showTitleInput() }
        (add_title as EditText).setOnEditorActionListener { p0, actionId, keyEvent ->
            if (actionId == IME_ACTION_DONE && keyEvent.keyCode == KEYCODE_ENTER) {
                addItemIfFilled()
                true
            } else false
        }
    }

    private fun showTitleInput() {
        add_title.visible()
        add_title.showKeyboard()
        add_icon.setOnClickListener { addItemIfFilled() }
    }

    private fun addItemIfFilled() {
        if (add_title.text.isNotEmpty()) onAddClicked(add_title.text.toString())
    }
}