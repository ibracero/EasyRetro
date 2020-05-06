package com.easyretro.ui

import android.view.KeyEvent.KEYCODE_ENTER
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.*
import kotlinx.android.synthetic.main.item_add.*
import timber.log.Timber


class AddItemViewHolder(
    parent: ViewGroup,
    private val onAddClicked: (String) -> Unit,
    type: ItemType,
    hasMoreItems: Boolean? = null
) : BaseViewHolder(parent.inflate(R.layout.item_add)) {

    enum class ItemType {
        RETRO,
        STATEMENT
    }

    init {
        containerView.setOnClickListener { showTitleInput() }

        (add_title as EditText).setOnEditorActionListener { p0, actionId, keyEvent ->
            if (actionId == IME_ACTION_DONE && keyEvent.keyCode == KEYCODE_ENTER) {
                addItemIfFilled()
                true
            } else false
        }

        setupVariant(type, hasMoreItems)
    }

    fun bindResult(success: Boolean) {
        loading.gone()
        add_icon.visible()
        if (success) add_title.setText("")
    }

    fun bindLockMode(retroLocked: Boolean) {
        Timber.d("Retro lock update: $retroLocked")
        if (retroLocked) itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        else itemView.layoutParams =
            RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        containerView.visibleOrGone(!retroLocked)
    }

    private fun setupVariant(type: ItemType, hasMoreItems: Boolean?) {
        val hint = if (type == ItemType.RETRO) containerView.context.getString(R.string.add_retro_hint)
        else containerView.context.getString(R.string.add_statement_hint)
        add_title.hint = hint

        create_label.visibleOrGone(type == ItemType.RETRO)
        welcome_label.visibleOrGone(type == ItemType.RETRO)
        choose_label.visibleOrGone(type == ItemType.RETRO && hasMoreItems == true)
        if (type == ItemType.STATEMENT) itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
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