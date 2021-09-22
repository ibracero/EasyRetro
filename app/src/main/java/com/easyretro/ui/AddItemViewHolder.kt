package com.easyretro.ui

import android.view.KeyEvent.KEYCODE_ENTER
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.easyretro.R
import com.easyretro.common.BaseViewHolder
import com.easyretro.common.extensions.*
import com.easyretro.databinding.ItemAddBinding


class AddItemViewHolder(
    parent: ViewGroup,
    private val onAddClicked: (String) -> Unit,
    type: ItemType
) : BaseViewHolder(parent.inflate(R.layout.item_add)) {

    enum class ItemType {
        RETRO,
        STATEMENT
    }

    val binding = ItemAddBinding.bind(itemView)

    init {
        itemView.setOnClickListener { showTitleInput() }

        binding.addTitle.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == IME_ACTION_DONE && keyEvent.keyCode == KEYCODE_ENTER) {
                addItemIfFilled()
                true
            } else false
        }

        setupVariant(type)
    }

    fun bindResult(success: Boolean) {
        with(binding) {
            loading.gone()
            addIcon.visible()
            if (success) addTitle.setText("")
        }
    }

    fun bindLockMode(retroProtected: Boolean) {
        if (retroProtected) itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        else itemView.layoutParams =
            RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupVariant(type: ItemType) {
        with(binding) {
            val hint = if (type == ItemType.RETRO) itemView.context.getString(R.string.add_retro_hint)
            else itemView.context.getString(R.string.add_statement_hint)
            addTitle.hint = hint

            createLabel.visibleOrGone(type == ItemType.RETRO)
            welcomeLabel.visibleOrGone(type == ItemType.RETRO)
        }
    }

    private fun showTitleInput() {
        binding.addTitle.visible()
        binding.addTitle.showKeyboard()
        binding.addIcon.setOnClickListener { addItemIfFilled() }
    }

    private fun addItemIfFilled() {
        with(binding) {
            if (binding.addTitle.text.isNotEmpty()) {
                onAddClicked(binding.addTitle.text.toString())
                loading.visible()
                binding.addIcon.invisible()
            }
        }
    }
}