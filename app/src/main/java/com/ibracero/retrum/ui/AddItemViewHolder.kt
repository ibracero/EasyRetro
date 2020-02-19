package com.ibracero.retrum.ui

import android.view.KeyEvent.KEYCODE_ENTER
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import com.ibracero.retrum.R
import com.ibracero.retrum.common.*
import kotlinx.android.synthetic.main.item_add.*

class AddItemViewHolder(
    parent: ViewGroup,
    private val onAddClicked: (String) -> Unit,
    type: ItemType
) : BaseViewHolder(parent.inflate(R.layout.item_add)) {

    enum class ItemType {
        RETRO,
        STATEMENT
    }

    init {
        containerView.setOnClickListener { showTitleInput() }
        val hint = if (type == ItemType.RETRO) containerView.context.getString(R.string.add_retro_hint)
        else containerView.context.getString(R.string.add_statement_hint)

        add_title.hint = hint

        (add_title as EditText).setOnEditorActionListener { p0, actionId, keyEvent ->
            if (actionId == IME_ACTION_DONE && keyEvent.keyCode == KEYCODE_ENTER) {
                addItemIfFilled()
                true
            } else false
        }
    }

    fun bindResult(success: Boolean) {
        loading.gone()
        add_icon.visible()
        if (success) add_title.setText("")
    }

    private fun showTitleInput() {
        add_title.visible()
        add_title.showKeyboard()
        add_icon.setOnClickListener { addItemIfFilled() }
    }

    private fun addItemIfFilled() {
        if (add_title.text.isNotEmpty()) {
            onAddClicked(add_title.text.toString())
            loading.visible()
            add_icon.invisible()
        }
    }
}